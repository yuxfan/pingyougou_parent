package com.jlb.core.service.seckill;

import com.alibaba.dubbo.config.annotation.Service;
import com.jlb.core.dao.seckill.SeckillGoodsDao;
import com.jlb.core.pojo.seckill.SeckillGoods;
import com.jlb.core.pojo.seckill.SeckillGoodsQuery;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
@Service
public class SeckillGoodsServiceImpl implements SeckillGoodsService {

    @Resource
    private SeckillGoodsDao seckillGoodsDao;

    @Resource
    private RedisTemplate redisTemplate;
    /**
     * 查询所有秒杀商品列表
     * 并添加到缓存中
     * @return
     */
    @Override
    public List<SeckillGoods> findList() {
        //从缓存中获取所有的值
        List<SeckillGoods> seckillGoodsList = redisTemplate.boundHashOps("seckillGoods").values();
        if (seckillGoodsList == null || seckillGoodsList.size() == 0) {    //判断为空
            //根据条件查询秒杀商品
            SeckillGoodsQuery query = new SeckillGoodsQuery();
            SeckillGoodsQuery.Criteria criteria = query.createCriteria();
            criteria.andStatusEqualTo("1");    //状态为审核的
            criteria.andNumGreaterThan(0);  //库存大于0的
            criteria.andStartTimeLessThanOrEqualTo(new Date());  //秒杀开始时间小于等于当前时间
            criteria.andEndTimeGreaterThan(new Date());  //结束时间大于当前时间
            seckillGoodsList = seckillGoodsDao.selectByExample(query);
            //遍历集合,存入缓存
            for (SeckillGoods seckillGoods : seckillGoodsList) {
                redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(), seckillGoods);
            }
        }
        return seckillGoodsList;
    }

    /**
     * 根据id从Redis在中取商品详情
     * @param id
     * @return
     */
    @Override
    public SeckillGoods findOneFromRedis(Long id) {
        return (SeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(id);
    }
}
