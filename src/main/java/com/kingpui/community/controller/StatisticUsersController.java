package com.kingpui.community.controller;

import com.kingpui.community.service.StatisticsUsers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.jws.WebParam;
import java.util.Date;

@Controller
public class StatisticUsersController {
    @Autowired
    private StatisticsUsers statisticsUsers;

    // 统计页面
    @RequestMapping(path = "/data",method = {RequestMethod.GET,RequestMethod.POST})
    public String getStaticsUsersPage(){
        return "/site/admin/data";
    }

    // 统计网站UV
    @RequestMapping(path = "/data/uv",method = RequestMethod.POST)
    public String getUV(@DateTimeFormat(pattern = "yyyy-MM-dd")Date start,
                        @DateTimeFormat(pattern = "yyyy-MM-dd")Date end, Model model){
        long uv = statisticsUsers.calculateUV(start,end);
        model.addAttribute("uvResult",uv);
        model.addAttribute("uvStartDate",start);
        model.addAttribute("uvEndDate",end);
        return "forward:/data";
    }

    // 统计活跃用户
    @RequestMapping(path = "/data/dau",method = RequestMethod.POST)
    public String getDAU(@DateTimeFormat(pattern = "yyyy-MM-dd")Date start,
                         @DateTimeFormat(pattern = "yyyy-MM-dd")Date end, Model model){
        long dau = statisticsUsers.calculateDAU(start,end);
        model.addAttribute("dauResult",dau);
        model.addAttribute("dauStartDate",start);
        model.addAttribute("dauEndDate",end);
        return "forward:/data";
    }

}
