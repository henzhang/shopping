package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.dao.SkuEsMapper;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.SkuService;
import entity.Result;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;

/****
 * @Author:henzhang
 * @Description:
 *****/
@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private SkuEsMapper skuEsMapper;

    /**
     * ElasticsearchTemplate 可以实现索引库的增删改查
     */
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    /**
     * 多条件搜索
     *
     * @param searchMap
     * @return
     */
    @Override
    public Map search(Map<String, String> searchMap) {
        // 条件封装
        NativeSearchQueryBuilder nativeSearchQueryBuilder = buildBasicQuery(searchMap);

        // 集合搜索
        Map<String, Object> resultMap = searchList(nativeSearchQueryBuilder);

        // 分组搜索
        Map<String, Object> groupMap = searchGroupList(nativeSearchQueryBuilder, searchMap);
        resultMap.putAll(groupMap);

        return resultMap;
    }

    /**
     * 条件封装
     *
     * @param searchMap
     * @return
     */
    private NativeSearchQueryBuilder buildBasicQuery(Map<String, String> searchMap) {
        //NativeSearchQuery
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();

        // Bool Query  (must , must_not , should)
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        if (searchMap != null && searchMap.size() > 0) {

            // 关键字   需要分词搜索
            String keywords = searchMap.get("keywords");
            if (!StringUtils.isEmpty(keywords)) {  // 如果关键字不为空，则搜索关键字
                boolQueryBuilder.must(QueryBuilders.queryStringQuery(keywords).field("name"));
            }

            // 分类    不需要分词搜索
            if (!StringUtils.isEmpty(searchMap.get("category"))) {
                boolQueryBuilder.must(QueryBuilders.termQuery("categoryName", searchMap.get("category")));
            }

            // 品牌    不需要分词搜索
            if (!StringUtils.isEmpty(searchMap.get("brand"))) {
                boolQueryBuilder.must(QueryBuilders.termQuery("brandName", searchMap.get("brand")));
            }

            // 规格过滤实现  : spec_网络=联通3G&spec_颜色=红      不需要分词搜索
            for (Map.Entry<String, String> entry : searchMap.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith("spec_")) {
                    // spec_网络 -> specMap.网络.keyword
                    boolQueryBuilder.must(QueryBuilders.termQuery("specMap." + key.substring(5) + ".keyword", entry.getValue()));
                }
            }

            /**
             * 价格过滤实现
             * price : 0-500元 500-1000元 1000-1500元 1500-2000元 2000-2500元 2500-3000元 3000元以上
             * -> 0-500 500-1000 1000-1500 1500-2000 2000-2500 2500-3000 3000
             * -> [0,500] [500,1000] ... [3000]
             * -> [x,y]  x 一定不为null , y 有可能为null
             * prices[0] != null   price > prices[0]
             * prices[1] != null   price <= prices[1]
             */

            String price = searchMap.get("price");
            if (!StringUtils.isEmpty(price)) {
                price = price.replace("元", "").replaceAll("以上", "");
                String[] prices = price.split("-");
                if (prices != null && prices.length > 0) {
                    // price > prices[0]
                    boolQueryBuilder.must(QueryBuilders.rangeQuery("price").gt(Integer.valueOf(prices[0])));
                    if (prices.length == 2) {
                        // price <= prices[1]
                        boolQueryBuilder.must(QueryBuilders.rangeQuery("price").lte(Integer.valueOf(prices[1])));
                    }
                }
            }

            /**
             * 排序实现
             *
             * 销量排序 ： 需要指定时间范围 月销量/季度销量
             * 新品排序 ： 根据创建时间
             * 评价排序 ： 根据评论总数排序
             * 价格排序 ： 根据价格
             */
            String sortField = searchMap.get("sortField"); // 指定排序的域
            String sortRule = searchMap.get("sortRule"); // 指定排序的规则
            if (!StringUtils.isEmpty(sortField) && !StringUtils.isEmpty(sortRule)) {
                nativeSearchQueryBuilder.withSort(
                        new FieldSortBuilder(sortField)
                                .order(SortOrder.valueOf(sortRule)));
            }

        }
        // 分页 如果用户不传分页参数 则默认是第一页
        Integer pageNum = converterPage(searchMap); //默认第一页
        Integer size = 30; // 默认每页30条
        nativeSearchQueryBuilder.withPageable(PageRequest.of(pageNum - 1, size));

        nativeSearchQueryBuilder.withQuery(boolQueryBuilder);
        return nativeSearchQueryBuilder;
    }

    /**
     * 接受前端传递的分页参数
     *
     * @param searchMap
     * @return
     */
    public Integer converterPage(Map<String, String> searchMap) {
        if (searchMap != null) {
            String pageNum = searchMap.get("pageNum");
            try {
                return Integer.parseInt(pageNum);
            } catch (NumberFormatException e) {
            }
        }
        return 1;
    }

    /**
     * 集合搜索
     *
     * @param nativeSearchQueryBuilder
     * @return
     */
    private Map<String, Object> searchList(NativeSearchQueryBuilder nativeSearchQueryBuilder) {
        // 添加高亮
        HighlightBuilder.Field field = new HighlightBuilder.Field("name"); // 指定高亮域
        //前缀
        field.preTags("<em style=\"color:red\">");
        //后缀
        field.postTags("</em>");
        //碎片长度  即关键词数据的长度  比如name2000个字符，不可能所有的都高亮，截取100个字符变为高亮
        field.fragmentSize(100);
        nativeSearchQueryBuilder.withHighlightFields(field);

//        AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);
        AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(
                nativeSearchQueryBuilder.build(),   // 封装的条件
                SkuInfo.class,                      // 数据集要转换的类型
                new SearchResultMapper() {          // 执行搜索后，数据结果集合封装到该对象中.
                    @Override
                    public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                        List<T> resultList = new ArrayList<T>();
                        // 1. 获取所有数据 (高亮数据+非高亮数据)
                        for (SearchHit hit : searchResponse.getHits()) {
                            // 2. 获取非高亮数据
                            SkuInfo skuInfo = JSON.parseObject(hit.getSourceAsString(), SkuInfo.class);
                            // 3. 获取高亮数据
                            HighlightField highlightField = hit.getHighlightFields().get("name");
                            if (highlightField != null && highlightField.getFragments() != null) {
                                // 读取高亮数据
                                Text[] fragments = highlightField.getFragments();
                                StringBuffer stringBuffer = new StringBuffer();
                                for (Text fragment : fragments) {
                                    stringBuffer.append(fragment.toString());
                                }
                                // 4. 高亮数据替换非高亮数据
                                skuInfo.setName(stringBuffer.toString());
                            }
                            resultList.add((T) skuInfo);
                        }
                        /**
                         * List<T> content : 搜索的集合数据
                         * Pageable pageable : 分页对象信息
                         * long total : 搜索记录的总条数
                         *
                         */
                        return new AggregatedPageImpl<T>(resultList, pageable, searchResponse.getHits().getTotalHits());
                    }
                }
        );

        List<SkuInfo> content = page.getContent(); // 获取数据结果集
        long totalElements = page.getTotalElements(); // 获取数据总条数
        int totalPages = page.getTotalPages(); // 获取数据总页数

        NativeSearchQuery query = nativeSearchQueryBuilder.build();
        Pageable pageable = query.getPageable();
        int pageNumber = pageable.getPageNumber(); // 获取当前数据页
        int pageSize = pageable.getPageSize(); // 获取一页数据size

        // 封装一个map
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("rows", content);
        resultMap.put("total", totalElements);
        resultMap.put("totalPages", totalPages);
        resultMap.put("pageNumber", pageNumber);
        resultMap.put("pageSize", pageSize);

        return resultMap;
    }

    /**
     * 分组查询 : 分类分组、品牌分组、规格分组
     *
     * @param nativeSearchQueryBuilder
     * @param searchMap
     * @return
     */
    private Map<String, Object> searchGroupList(NativeSearchQueryBuilder nativeSearchQueryBuilder, Map<String, String> searchMap) {
        /**
         * addAggregation() : 添加一个聚合操作
         * AggregationBuilders.terms  arg1 : 取别名
         * AggregationBuilders.terms  arg2 : 表示对哪个域进行分组查询
         *
         * 当用户选择了分类，将分类作为搜索条件，则不需要在进行"分类分组查询"，因为"分类分组查询"用于显示分类搜索条件的.
         * 品牌也类似
         *
         * spec.keyword      带.keyword 后缀表示不分词的意思
         * 默认分组后显示10条， 所以设置size(10000)，这样等同于取消限制， 就可以获取所有的规格spec数据.
         */
        if (searchMap == null || StringUtils.isEmpty(searchMap.get("category"))) {
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuCategoryGroup").field("categoryName"));
        }
        if (searchMap == null || StringUtils.isEmpty(searchMap.get("brand"))) {
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuBrandGroup").field("brandName"));
        }
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuSpec").field("spec.keyword").size(10000));
        AggregatedPage<SkuInfo> aggregationPage = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);


        Map<String, Object> groupResultMap = new HashMap<>();
        /**
         * 获取分组数据
         *  aggregationPage.getAggregations() ： 获取的是集合，因为可以根据多个域分组
         */
        if (searchMap == null || StringUtils.isEmpty(searchMap.get("category"))) {
            StringTerms categoryTerms = aggregationPage.getAggregations().get("skuCategoryGroup");
            List<String> categoryList = getGroupList(categoryTerms);
            groupResultMap.put("categoryList", categoryList);
        }

        if (searchMap == null || StringUtils.isEmpty(searchMap.get("brand"))) {
            StringTerms brandTerms = aggregationPage.getAggregations().get("skuBrandGroup");
            List<String> brandList = getGroupList(brandTerms);
            groupResultMap.put("brandList", brandList);
        }

        StringTerms specTerms = aggregationPage.getAggregations().get("skuSpec");
        List<String> specList = getGroupList(specTerms);
        Map<String, Set<String>> specMap = putAllSpec(specList);
        groupResultMap.put("specList", specMap);

        return groupResultMap;
    }

    private List<String> getGroupList(StringTerms groupTerms) {
        List<String> groupList = new ArrayList<>();
        for (StringTerms.Bucket temp : groupTerms.getBuckets()) {
            groupList.add(temp.getKeyAsString());
        }
        return groupList;
    }


    /**
     * 规格汇总合并
     *
     * @param specList
     * @return
     */
    private Map<String, Set<String>> putAllSpec(List<String> specList) {
        Map<String, Set<String>> resultMap = new HashMap<>();
        for (String temp : specList) {
            Map<String, String> specMap = JSON.parseObject(temp, Map.class);
            for (Map.Entry<String, String> entry : specMap.entrySet()) {
                String key = entry.getKey();     //规格名称
                String value = entry.getValue();  // 规格值
                Set<String> specSet = resultMap.get(key);
                if (specSet == null) {
                    specSet = new HashSet<>();
                }
                specSet.add(value);
                resultMap.put(key, specSet);
            }
        }
        return resultMap;
    }


    /**
     * 导入数据到索引库中
     */
    @Override
    public void importSku() {
        // 1. 调用Feign 查询List<Sku>s
        Result<List<Sku>> skuResult = skuFeign.findAll();

        /**
         * 2. 将List<Sku>转换成List<SkuInfo>
         * List<Sku> -> [{sku json}] -> List<skuInfo>
         */
        List<SkuInfo> skuInfoList = JSON.parseArray(JSON.toJSONString(skuResult.getData()), SkuInfo.class);

        for (SkuInfo temp : skuInfoList) {
            // 获取spec -> String(map)-> map {"电视音响效果":"小影院","电视屏幕尺寸":"20英寸","尺码":"165"}
            Map<String, Object> specMap = JSON.parseObject(temp.getSpec(), Map.class);
            // 如果要生成一个动态的域，则将该域转换成一个Map<String,Object>对象中即可，该Map<String,Object>对象中的key会生成一个域，域的名字为该map的key.
            temp.setSpecMap(specMap);
        }

        // 3. 调用Dao实现批量导入
        skuEsMapper.saveAll(skuInfoList);

    }

}
