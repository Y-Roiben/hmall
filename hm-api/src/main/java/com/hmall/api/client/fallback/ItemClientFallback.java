package com.hmall.api.client.fallback;


import com.hmall.api.client.ItemClient;
import com.hmall.api.dto.ItemDTO;
import com.hmall.api.dto.OrderDetailDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Slf4j
public class ItemClientFallback implements FallbackFactory<ItemClient> {

    @Override
    public ItemClient create(Throwable cause) {

        return new ItemClient() {
            @Override
            public List<ItemDTO> queryItemByIds(Collection<Long> ids) {
                log.info("查询商品服务失败，ids:{}", ids, cause);
                return Collections.emptyList();
            }

            @Override
            public void deductStock(List<OrderDetailDTO> items) {
                log.info("扣减商品库存失败，items:{}", items, cause);
                throw new RuntimeException("扣减商品库存失败");
            }
        };
    }
}
