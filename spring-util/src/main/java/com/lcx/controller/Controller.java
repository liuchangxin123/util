package com.lcx.controller;

/**
 * @ClassName:Controller
 * @Auther: liuchangxin
 * @Date: 2018/8/28 11:34
 * @Description: TODO(一句话描述此类作用)
 */
public class Controller {
    public static void main(String[] args) throws Exception {
//        //CODE_1
        Text text=new Text();
        text.setValue(1);

        Text text1=new Text();
        text1.setValue(2);

        swap1(text,text1);
        System.out.printf("a = %s, b = %s",text.getValue(),text1.getValue());
        System.out.println();
        swap2(text,text1);
        System.out.printf("a = %s, b = %s",text.getValue(),text1.getValue());
//        System.out.println("adsas");
    }
    public  static void swap2(Text text, Text text1){
        int temp=text.getValue();
        text.setValue(text1.getValue());
        text1.setValue(temp);
    }

    public static void swap1(Text text, Text text1){
        Text text2=text;
        text=text1;
        text1=text2;
    }
}
