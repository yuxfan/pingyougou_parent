package com.jlb.core.service.page;

import com.alibaba.dubbo.config.annotation.Service;
import com.jlb.core.dao.good.GoodsDao;
import com.jlb.core.dao.good.GoodsDescDao;
import com.jlb.core.dao.item.ItemCatDao;
import com.jlb.core.dao.item.ItemDao;
import com.jlb.core.pojo.good.Goods;
import com.jlb.core.pojo.good.GoodsDesc;
import com.jlb.core.pojo.item.Item;
import com.jlb.core.pojo.item.ItemQuery;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import javax.annotation.Resource;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemPageServiceImpl implements ItemPageService {

    @Value("${pagedir}")
    private String pagedir;

    @Resource
    private FreeMarkerConfig freeMarkerConfig;

    @Resource
    private GoodsDao goodsDao;

    @Resource
    private GoodsDescDao goodsDescDao;

    @Resource
    private ItemCatDao itemCatDao;

    @Resource
    private ItemDao itemDao;

    @Override
    public boolean genItemHtml(Long goodsId) throws Exception {
        //1,创建配置对象
        Configuration configuration = freeMarkerConfig.getConfiguration();
        //2,创建模板使用的数据集
        //2.1, 加载商品数据
        Map dataModel=new HashMap();
        Goods goods = goodsDao.selectByPrimaryKey(goodsId);
        dataModel.put("goods",goods);
        //2.2, 加载商品详情数据
        GoodsDesc goodsDesc = goodsDescDao.selectByPrimaryKey(goodsId);
        dataModel.put("goodsDesc",goodsDesc);
        //2.3, 加载面包屑
        String itemCat1 = itemCatDao.selectByPrimaryKey(goods.getCategory1Id()).getName();
        dataModel.put("itemCat1",itemCat1);
        String itemCat2 = itemCatDao.selectByPrimaryKey(goods.getCategory2Id()).getName();
        dataModel.put("itemCat2",itemCat2);
        String itemCat3 = itemCatDao.selectByPrimaryKey(goods.getCategory3Id()).getName();
        dataModel.put("itemCat3",itemCat3);
        //2.4, 页面生成 SKU 列表变量
        ItemQuery itemQuery=new ItemQuery();
        itemQuery.createCriteria().andGoodsIdEqualTo(goodsId);
        //状态为1 上架
        itemQuery.createCriteria().andStatusEqualTo("1");
        itemQuery.setOrderByClause("is_default desc");
        List<Item> items = itemDao.selectByExample(itemQuery);
        dataModel.put("itemList",items);
        //3,加载模板
        Template template = configuration.getTemplate("item.ftl");
        //4,创建输出对象 (创建一个 Writer 对象，一般创建一 FileWriter 对象，指定生成的文件名。)
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(pagedir+goodsId+".html"),"UTF-8");
        //5,输出
        template.process(dataModel,out);
        //6,关流
        out.close();
        return true;
    }
}
