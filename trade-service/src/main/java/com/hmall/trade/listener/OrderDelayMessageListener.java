package com.hmall.trade.listener;

import com.hmall.api.client.PayClient;
import com.hmall.api.dto.PayOrderDTO;
import com.hmall.trade.constants.MQConstants;
import com.hmall.trade.domain.po.Order;
import com.hmall.trade.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderDelayMessageListener {

    private final IOrderService orderService;
    private final PayClient payClient;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = MQConstants.DELAY_ORDER_QUEUE_NAME),
            exchange = @Exchange(name = MQConstants.DELAY_EXCHANGE_NAME),
            key = {MQConstants.DELAY_ORDER_KEY}
    ))
    public void listenOrderDelayQueue(Long orderId) {
        // 查询订单
        Order order = orderService.getById(orderId);
        // 查询订单状态，判断是否已经支付
        if (order== null || order.getStatus() != 1) {
            // 不存在已支付, 不做处理
            return;
        }
        // 未支付, 查询交易流水
        PayOrderDTO payOrderDTO = payClient.queryPayOrderByBizOrderNo(orderId);
        // 判断是否支付
        if (payOrderDTO != null && payOrderDTO.getStatus() == 3){
            // 已支付, 修改订单状态标记为已支付
            orderService.markOrderPaySuccess(orderId);
        }else {
            // 未支付
            orderService.cancelOrder(orderId);
        }


    }
}
