package com.jlb.core.service.vo;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.jlb.core.dao.good.BrandDao;
import com.jlb.core.dao.good.GoodsDao;
import com.jlb.core.dao.good.GoodsDescDao;
import com.jlb.core.dao.item.ItemCatDao;
import com.jlb.core.dao.item.ItemDao;
import com.jlb.core.dao.seller.SellerDao;
import com.jlb.core.entity.PageResult;
import com.jlb.core.pojo.good.Goods;
import com.jlb.core.pojo.good.GoodsDesc;
import com.jlb.core.pojo.good.GoodsQuery;
import com.jlb.core.pojo.item.Item;
import com.jlb.core.pojo.item.ItemQuery;
import com.jlb.core.vo.GoodsVo;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.transaction.annotation.Transactional;
import sun.management.Agent;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class GoodsVoServiceImpl implements GoodsVoService {
    @Resource
    private GoodsDao goodsDao;
    
    @Resource
    private GoodsDescDao goodsDescDao;

    @Resource
    private ItemCatDao itemCatDao;

    @Resource
    private BrandDao brandDao;

    @Resource
    private SellerDao sellerDao;

    @Resource
    private ItemDao itemDao;

    @Resource
    private SolrTemplate solrTemplate;

    //商品保存  保存之后 商品需要设置自增主键
    @Transactional
    @Override
    public void add(GoodsVo goodsVo) {
        //1.保存商品tb_goods
        Goods goods = goodsVo.getGoods();
        goodsDao.insertSelective(goods);
        //2.保存商品详情 tb_goods_desc
        GoodsDesc goodsDesc = goodsVo.getGoodsDesc();
        goodsDesc.setGoodsId(goods.getId());  //设置外键
        goodsDescDao.insertSelective(goodsDesc);
        //3.保存商品对应的库存信息  tb_item
        //判断是否启用规格
        if ("1".equals(goods.getIsEnableSpec())){
            //启用规格,一个商品对应多个库存
            List<Item> itemList = goodsVo.getItemList();
            for (Item item : itemList) {
            //商品名称:spu名称+spu副标题+规格
                String title = goods.getGoodsName() + " " + goods.getCaption();
                String spec = item.getSpec();
                //map,set  问题
                Map<String,String> map = JSON.parseObject(spec, Map.class);
                Set<Map.Entry<String, String>> entries = map.entrySet();
                for (Map.Entry<String, String> entry : entries) {
                    title += " "+entry.getValue();
                }
                item.setTitle(title);
                setAtrributeForItem(goods,goodsDesc,item);
                itemDao.insertSelective(item);

            }
        }else{
            //不启用规格,那么一个商品对应一个库存
            Item item=new Item();
            item.setTitle(goods.getGoodsName()+" "+goods.getCaption());
            item.setPrice(goods.getPrice());
            item.setNum(8888);
            item.setIsDefault("1");
            item.setSpec("{}");
            setAtrributeForItem(goods,goodsDesc,item);
            itemDao.insertSelective(item);
        }

    }

    private void setAtrributeForItem(Goods goods, GoodsDesc goodsDesc, Item item) {
        //获取图片信息
        String itemImages = goodsDesc.getItemImages();
        List<Map> images = JSON.parseArray(itemImages, Map.class);
        if (images!=null && images.size()>0){
            String image = images.get(0).get("url").toString();
            item.setImage(image);
        }
        item.setCategoryid(goods.getCategory3Id());   //三级目录的id
        item.setStatus("1");
        item.setCreateTime(new Date());
        item.setUpdateTime(new Date());
        item.setGoodsId(goods.getId());
        item.setSellerId(goods.getSellerId());
        item.setCategory(itemCatDao.selectByPrimaryKey(goods.getCategory3Id()).getName());  //分类名称
        item.setBrand(brandDao.selectByPrimaryKey(goods.getBrandId()).getName());  //品牌名称
        item.setSeller(sellerDao.selectByPrimaryKey(goods.getSellerId()).getNickName());  //商家店铺名称

    }


    //商家管理分页查询
    @Override
    public PageResult search(Integer page, Integer rows, Goods goods) {
        PageHelper.startPage(page,rows);
        PageHelper.orderBy("id  desc");
        GoodsQuery goodsQuery=new GoodsQuery();
        if (goods.getSellerId()!=null && !"".equals(goods.getSellerId())){
            goodsQuery.createCriteria().andSellerIdEqualTo(goods.getSellerId());
        }
        Page<Goods> p = (Page<Goods>) goodsDao.selectByExample(goodsQuery);
        return new PageResult(p.getTotal(),p.getResult());
    }

    //商品管理修改之回显
    @Override
    public GoodsVo findOne(Long id) {
        GoodsVo goodsVo=new GoodsVo();
        //商品信息
        Goods goods = goodsDao.selectByPrimaryKey(id);
        goodsVo.setGoods(goods);
        //商品描述信息
        GoodsDesc goodsDesc = goodsDescDao.selectByPrimaryKey(id);
        goodsVo.setGoodsDesc(goodsDesc);
        //商品库存信息
        ItemQuery itemQuery=new ItemQuery();
        //商品的库存,所以需要和商品的id相等
        itemQuery.createCriteria().andGoodsIdEqualTo(id);
        List<Item> itemList = itemDao.selectByExample(itemQuery);
        goodsVo.setItemList(itemList);
        return goodsVo;
    }

    //商品管理修改之回显
    @Transactional
    @Override
    public void update(GoodsVo goodsVo) {
        //更新商品
        Goods goods = goodsVo.getGoods();
        goods.setAuditStatus("0");  //如果审核未通过的话,打回来状态还是未审核
        goodsDao.updateByPrimaryKeySelective(goods);
        //更新商品详情
        GoodsDesc goodsDesc = goodsVo.getGoodsDesc();
        goodsDescDao.updateByPrimaryKeySelective(goodsDesc);
        //更新商品对应的库存
        //先删除
        ItemQuery itemQuery=new ItemQuery();
        itemQuery.createCriteria().andGoodsIdEqualTo(goods.getId());
        itemDao.deleteByExample(itemQuery);
        //再插入
        //判断是否启用规格
        if("1".equals(goods.getIsEnableSpec())){  //开启规格
            List<Item> itemList = goodsVo.getItemList();
            for (Item item : itemList) {
                String title = goods.getGoodsName();  //spu名称
                Map<String,String> map = JSON.parseObject(item.getSpec(), Map.class);
                Set<Map.Entry<String, String>> entries = map.entrySet();  //遍历map集合的方式
                for (Map.Entry<String, String> entry : entries) {
                    title += " " + entry.getValue();   //entry中获取值 规格 与 spu名称拼接
                }
                item.setTitle(title);
                setAtrributeForItem(goods, goodsDesc, item);
                itemDao.updateByPrimaryKeySelective(item);
            }
        }else{
            //不启用规格,那么一个商品对应一个库存
            Item item=new Item();
            item.setTitle(goods.getGoodsName()+" "+goods.getCaption());
            item.setPrice(goods.getPrice());
            item.setNum(8888);
            item.setIsDefault("1");
            item.setSpec("{}");
            setAtrributeForItem(goods,goodsDesc,item);
            itemDao.insertSelective(item);
        }
    }

    //运营商商品待审核之 分页查询
    @Override
    public PageResult searchByManager(Integer page, Integer rows, Goods goods) {
        PageHelper.startPage(page,rows);
        PageHelper.orderBy("id desc");
        GoodsQuery goodsQuery=new GoodsQuery();
        GoodsQuery.Criteria criteria=goodsQuery.createCriteria();
        if (goods.getAuditStatus()!=null && !"".equals(goods.getAuditStatus().trim())){
            criteria.andAuditStatusEqualTo(goods.getAuditStatus().trim());
        }
        criteria.andIsDeleteIsNull(); //查询未删除的
        Page<Goods> p = (Page<Goods>) goodsDao.selectByExample(goodsQuery);
        return new PageResult(p.getTotal(),p.getResult());
    }

    //商品审核   就是通过id(复选框)来进行状态的修改
    @Transactional
    @Override
    public void updateStatus(Long[] ids, String status) {
        if(ids != null && ids.length>0){
            Goods goods=new Goods();
            goods.setAuditStatus(status);
            for (Long id : ids) {
                goods.setId(id);
                goodsDao.updateByPrimaryKeySelective(goods);
                if ("1".equals(status)){
                    //TODO 将审核通过对的商品保存到索引库中(solr索引库)
                    dataImportToSolr();
                    //TODO 生成该商品的静态页面(freemaker技术)
                }
            }
        }
    }
    //将数据库数据导入到索引库中
    private void dataImportToSolr() {
        List<Item> itemList = itemDao.selectByExample(null);
        if (itemList != null && itemList.size()>0){
            for (Item item : itemList) {
                //设置动态字段
                String spec = item.getSpec();
                Map specMap = JSON.parseObject(spec, Map.class);
                item.setSpecMap(specMap);
            }
            //手动提交
            solrTemplate.saveBeans(itemList);
            solrTemplate.commit();
        }
    }

    //商品审核之 删逻辑除[更新状态(根据id(id用[]来接收))]
    @Transactional
    @Override
    public void delete(Long[] ids) {
        if(ids != null && ids.length>0){
            Goods goods=new Goods();
            //设置已删除的状态为 1
            goods.setIsDelete("1");
            for (Long id : ids) {
                goods.setId(id);
                //更新数据库的状态   为 1 表示已在前台页面删除
                goodsDao.updateByPrimaryKeySelective(goods);
                //TODO 更新索引库
                //TODO 删除商品的静态详情页(可选)
            }
        }
    }
}
