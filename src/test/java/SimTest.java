public class SimTest {
    public static void main(String[] args) {
        class A{
            public A() {
                System.out.println("构建啦");
            }
            public void eat(){
                System.out.println("吃一个A");
            }
        }
        class B extends A{

        }

        class C extends B{}
        C c = new C();
        c.eat();
    }
}
