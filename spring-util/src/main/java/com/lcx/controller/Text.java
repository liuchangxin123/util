package com.lcx.controller;

import java.util.HashMap;

/**
 * @ClassName:Text
 * @Auther: liuchangxin
 * @Date: 2018/8/28 11:31
 * @Description: TODO(一句话描述此类作用)
 */
public class Text {
    private int value;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public static void main(String[] args) {
        HashMap<String,Object> map=new HashMap<>();
        String s=null;
        map.put("aaa",s.toString());
        map.get("");
    }

}

