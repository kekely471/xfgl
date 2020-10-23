package com.tonbu.web;



/**
 * @program: xfgl
 * @author: keke
 * @create: 2020-01-20 15:51
 **/


    interface Eatable{
        void taste() ;
    }

    interface Flyable{
        void fly(String weather);
    }

    interface Addable{
        int add(int a,int b);
//        int beef(String aa,String bb);

    }

    public class LambdaTest{
        //调用该方法需要Eatable对象
        public void eat (Eatable e){
            System.out.println(e) ;
            e.taste();
        }

        //调用该方法需要Flyable对象
        public void drive (Flyable f){
            System.out.println("我正在驾驶: " + f);
            f.fly(" [碧空如洗的晴日] ");

        }

        //调用该方法需要Addable对象
        public void test (int i,int j,Addable add){
            System.out .println(i+"与"+j+"的和为:"+ add.add(i, j));
//            System.out .println(i+"与"+j+"的和为:"+ add.beef(i+"", j+""));

        }

        public static void main(String[] args) {
            LambdaTest lq = new LambdaTest () ;
// Lambda表达式的代码块只有一-条语句，可以省略花括号
//            lq.eat(()-> System.out.println ("苹果的味道不错! "));
// Lambda 表达式的形参列表只有一一个形参，可以省略圆括号
/*        lq.drive(beef ->
                {
                        System. out . println("今天天气是:"+ beef);
                        System. out . println ("直升机飞行平稳") ;
    });*/
/*lq.drive(weather -> {
    System.out.println("今天天气是："+weather);
    System.out.println("直升机飞行平稳");
});*/
// Lambda表达式的代码块只有条语句， 可以省略花括号
//代码块中只有一条语句，即使该表达式需要返回值，也可以省略return关键字
        lq.test(6,5,(a,b)->a + b);
        }
    }








