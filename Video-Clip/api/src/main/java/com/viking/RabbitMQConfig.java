//package com.viking;
//
//import org.springframework.amqp.core.*;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//
//@Configuration
//public class RabbitMQConfig {
//    /**
//     * 1.定义交换机
//     * 2.定义队列
//     * 3.创建交换机
//     * 4.创建队列
//     * 5.队列和交换机绑定
//     */
//    public static final String EXCHANGE_MSG = "exchage_msg";
//
//    public static final String QUEUE_SYS_MSG = "queue_sys_msg";
//
//    @Bean(EXCHANGE_MSG)
//    public Exchange exchange(){
//        return ExchangeBuilder.topicExchange(EXCHANGE_MSG).durable(true).build();
//    }
//
//    @Bean(QUEUE_SYS_MSG)
//    public Queue queue(){
//        return new Queue(QUEUE_SYS_MSG , true);
//    }
//
//    @Bean
//    public Binding binding(@Qualifier(EXCHANGE_MSG) Exchange exchange,
//                           @Qualifier(QUEUE_SYS_MSG) Queue queue){
//        return BindingBuilder.
//                bind(queue).
//                to(exchange).
//                with("sys.msg.*").noargs();
//    }
//}
