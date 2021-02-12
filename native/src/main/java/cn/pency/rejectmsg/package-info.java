/**
 * @author pency

 * 类说明：消息的拒绝
 * Reject在拒绝消息时，可以使用requeue标识，
 * 告诉RabbitMQ是否需要重新发送给别的消费者。
 * 不重新发送，一般这个消息就会被RabbitMQ丢弃。
 * Reject一次只能拒绝一条消息。
 * Nack则可以一次性拒绝多个消息
 * 通过RejectRequeuConsumer可以看到当requeue参数设置为true时，消息发生了重新投递
 */
package cn.pency.rejectmsg;
