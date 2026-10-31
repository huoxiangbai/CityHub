package com.hmdp.config;

import com.hmdp.entity.Shop;
import com.hmdp.service.IShopService;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherOrderService;
import com.hmdp.utils.RedisConstants;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

/** 本地开发数据衔接：建好 MySQL 后即可使用附近商铺和已有秒杀券。 */
@Component
@Profile("local")
public class LocalDataInitializer implements ApplicationRunner {
    @Resource private IShopService shops;
    @Resource private ISeckillVoucherService vouchers;
    @Resource private IVoucherOrderService orders;
    @Resource private StringRedisTemplate redis;

    @Override
    public void run(ApplicationArguments args) {
        for (Shop shop : shops.list()) {
            if (shop.getX() != null && shop.getY() != null) {
                redis.opsForGeo().add(RedisConstants.SHOP_GEO_KEY + shop.getTypeId(),
                        new Point(shop.getX(), shop.getY()), shop.getId().toString());
            }
        }
        vouchers.list().forEach(voucher -> {
            // 保留 Redis 已有的预占库存，不在重启时覆盖。
            redis.opsForValue().setIfAbsent(RedisConstants.SECKILL_STOCK_KEY + voucher.getVoucherId(),
                    voucher.getStock().toString());
        });
        orders.list().forEach(order -> redis.opsForSet().add("seckill:order:" + order.getVoucherId(),
                order.getUserId().toString()));
    }
}
