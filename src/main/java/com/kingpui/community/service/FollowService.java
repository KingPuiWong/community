package com.kingpui.community.service;

import com.kingpui.community.entity.User;
import com.kingpui.community.util.CommunityConstant;
import com.kingpui.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowService implements CommunityConstant {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private  UserService userService;

    public void follow(int userId,int entityType,int entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String followeekey = RedisKeyUtil.getFolloweeKey(userId,entityType);
                String followerkey = RedisKeyUtil.getFollowerKey(entityType,entityId);

                redisOperations.multi();

                redisOperations.opsForZSet().add(followeekey,entityId,System.currentTimeMillis());
                redisOperations.opsForZSet().add(followerkey,userId,System.currentTimeMillis());
                return redisOperations.exec();
            }
        });
    }

    public void unfolow(int userId,int entityType,int entityId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId,entityType);
                String followerkey = RedisKeyUtil.getFollowerKey(entityType,entityType);

                redisOperations.multi();

                redisOperations.opsForZSet().remove(followeeKey,entityId);
                redisOperations.opsForZSet().remove(followerkey,userId);

                return redisOperations.exec();
            }
        });
    }

    // 查询关注的实体的数量
    public long findFolloweeCount(int userId, int entityType) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }


    // 查询实体的粉丝的数量
    public long findFollowerCount(int entityType,int entityId){
        String followerKey = RedisKeyUtil.getFollowerKey(entityType,entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    //查询当前用户是否已关注该实体
    public boolean hasFollowed(int userId,int entityType,int entityId){
        String followeekey = RedisKeyUtil.getFolloweeKey(userId,entityType);
        return redisTemplate.opsForZSet().score(followeekey,entityId) != null;
    }

    // 查询某用户关注的人
    public List<Map<String,Object>> findFollowees(int userId,int offset,int limit){
        String followeekey = RedisKeyUtil.getFolloweeKey(userId,ENTITY_TYPE_USER);
        Set<Integer>  targetIds = redisTemplate.opsForZSet().reverseRange(followeekey,offset,offset+limit);

        if (targetIds == null){
            return  null;
        }

        List<Map<String,Object>> list = new ArrayList<>();
        for (Integer targetId:targetIds){
                Map<String,Object> map = new HashMap<>();
                User user = userService.findUserById(targetId);
                map.put("user",user);
                Double score = redisTemplate.opsForZSet().score(followeekey,targetId);
                map.put("followTime",new Date(score.longValue()));
                list.add(map);
        }
        return  list;
    }


    // 查询某用户的粉丝
    public List<Map<String,Object>> findFollowers(int userId,int offset,int limit){
        String followerKey =  RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER,userId);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey,offset,offset+limit-1);

        if (targetIds == null){
            return null;
        }

        List<Map<String,Object>> list = new ArrayList<>();
        for (Integer targetId:targetIds){
            Map<String,Object> map = new HashMap<>();
            User user = userService.findUserById(targetId);
            map.put("user",user);
            Double score = redisTemplate.opsForZSet().score(followerKey,targetId);
            map.put("followTime",new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }


}
