package com.tonbu.web;

import com.tonbu.protectedweb.StringU;

public class StringZ {

    public static void main(String[] args) {
        new StringU().publicstr();
        new StringU(){
            void prostr(){
                super.protectedstr();
            }
        }.prostr();

        new StringU(){
            void pristr(){
                System.out.println("pristr");
            }
        }.pristr();


        String a= "12313";
        System.out.println(a.charAt(0));

    }
}
