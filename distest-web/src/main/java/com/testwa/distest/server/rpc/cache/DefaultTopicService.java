package com.testwa.distest.server.rpc.cache;import io.rpc.testwa.push.Status;import io.rpc.testwa.push.TopicInfo;import java.util.HashSet;import java.util.Set;import java.util.concurrent.ConcurrentHashMap;public class DefaultTopicService implements ITopicService {	private ConcurrentHashMap<String, Set<String>> topics = new ConcurrentHashMap<>();		public Status subscribeTopic(TopicInfo topicInfo) {		String id = topicInfo.getClientInfo().getDeviceId();		String topic = topicInfo.getTopicName();		Set<String> ids = topics.computeIfAbsent(topic, k -> new HashSet<>());		ids.add(id);		return Status.newBuilder().setStatus("OK").build();	}	public Status cancelTopic(TopicInfo topicInfo) {		String topic = topicInfo.getTopicName();		Set<String> ids  = topics.get(topic);		if(ids==null){			return Status.newBuilder().setStatus("OK").build();		}		ids.remove(topicInfo.getClientInfo().getDeviceId());		return Status.newBuilder().setStatus("OK").build();	}	public Set<String> getClientId(String topic) {		return topics.get(topic);	}}