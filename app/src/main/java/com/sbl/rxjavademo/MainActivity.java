package com.sbl.rxjavademo;

import android.support.annotation.MainThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.internal.schedulers.NewThreadScheduler;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG =  "songbl";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button1).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);
        findViewById(R.id.button3).setOnClickListener(this);
        findViewById(R.id.button4).setOnClickListener(this);
        findViewById(R.id.button5).setOnClickListener(this);
        findViewById(R.id.button6).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button1:
                testOne();
                break;
            case R.id.button2:
                testTwo();
                break;
            case R.id.button3:
                testThree();
                break;
            case R.id.button4:
                testFour();
                break;
            case R.id.button5:
                testFive();
                break;
            case R.id.button6:
                testSix();
                break;
        }
    }


    //入门操作
    public void testOne(){
        //创建上游
        Observable<Integer> observable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                emitter.onNext(1);
                emitter.onNext(2);
                emitter.onNext(3);
                emitter.onComplete();
            }
        });
        //创建下游
        Observer <Integer> observer = new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.i(TAG,"onSubscribe====");
            }

            @Override
            public void onNext(Integer integer) {
                Log.i(TAG,"===="+integer);
            }

            @Override
            public void onError(Throwable e) {
                Log.i(TAG, "error");
            }

            @Override
            public void onComplete() {
                Log.i(TAG, "complete");
            }
        };
        //将上游下游绑定
        observable.subscribe(observer);

    }


    //切断水管，链式操作，发送接收情况测试
    public void testTwo(){
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {//上游的发送和下游的接收处理是没有关系的，在不同的线程，上游只管发送...
                Log.d(TAG, "emit 1");
                emitter.onNext(1);
                Log.d(TAG, "emit 2");
                emitter.onNext(2);
                Log.d(TAG, "emit 3");
                emitter.onNext(3);
                Log.d(TAG, "emit complete");//发送“完成”，上游继续发送，只是下游不再接受
                emitter.onComplete();
                Log.d(TAG, "emit 4");
                emitter.onNext(4);
            }
        }).subscribe(
//            new Observer<Integer>() {
//            private Disposable mDisposable;
//            private  int i;
//            @Override
//            public void onSubscribe(Disposable d) {
//                Log.i(TAG,"onSubscribe====");
//                mDisposable = d;
//            }
//
//            @Override
//            public void onNext(Integer integer) {
//                Log.i(TAG,"===="+integer);
//                i++;
//                if (i==2){//切断水管之后，下游不再接收，但是不影响上游的发送
//                    mDisposable.dispose();
//                    Log.d(TAG, "isDisposed : " + mDisposable.isDisposed());
//                }
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                Log.i(TAG, "error");
//            }
//
//            @Override
//            public void onComplete() {
//                Log.i(TAG, "complete");
//            }
//        }

                //方案二，subscribe的重载方法,多个
                /**
                 * public final Disposable subscribe() {}//接口类型引用onNest
                 public final Disposable subscribe(Consumer<? super T> onNext) {}
                 public final Disposable subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError) {}
                 public final Disposable subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Action onComplete) {}
                 public final Disposable subscribe(Consumer<? super T> onNext, Consumer<? super Throwable> onError, Action onComplete, Consumer<? super Disposable> onSubscribe) {}
                 public final void subscribe(Observer<? super T> observer) {}
                 */
                new Consumer<Integer>() {//这个是next接受的事件
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.i(TAG,"===="+integer);
                    }
                }, new Consumer<Throwable>() {//这个是异常出错的事件
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.i(TAG, "error");
                    }
                }
        );

    }


    //线程调度。默认情况下，上游下游在同一线程。上游在哪个线程发送事件下游就在哪个线程接受事件。//考虑情况：在子线程中做耗时操作，回到主线程中操作UI。
    public void testThree() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                Log.d(TAG, "emitter" +1);
                emitter.onNext(1);
                Log.d(TAG, "emitter" +2);
                emitter.onNext(2);
                Log.d(TAG, "Observable thread is : " + Thread.currentThread().getName());
            }
        }).subscribeOn(Schedulers.newThread())//我称它：绑定线程；上游发送，耗时操作，多次指定只有第一次有效
                .observeOn(Schedulers.io())//指定doOnNext的线程;不指定的话，会在发送线程中执行，先onNext之后执行它（doOnNext），在onNext
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Thread.sleep(2000);
                        Log.d(TAG, "doOnNext===Observer thread is :" + Thread.currentThread().getName());
                        //如果是person，修改属性，最后接受的person属性会发生改变，它执行完，最后的accpet才会执行
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())//绑定线程，下游接受线程.最后观察者在主线程
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.d(TAG, "Observer thread is :" + Thread.currentThread().getName());
                        Log.d(TAG, "onNext: " + integer);
                    }
                });
    }


    //操作符。map:作用是对上游发送的每一个事件应用一个函数，使得每一个事件按照指定函数变化
    public void testFour(){
        //map操作符   |感觉map操作符，只是简单的处理一下事件，并不能有效的转化发送事件
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                emitter.onNext(1);
                emitter.onNext(2);
                emitter.onNext(3);
            }
        }).map(new Function<Integer, String>() {//将发送事件类型Integer转化为String发送
            @Override
            public String apply(Integer integer) throws Exception {
                return "开始变身"+integer;
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Log.i(TAG,s);
            }
        });

    }


    //flapMap操作符。上游每发送一个事件，flatMap都将创建一个新的水管，然后发送转换之后新的事件，
    // 下游接收的也是这些新的水管发送的事件（不保证顺序，需要顺序的话，concatMap）
    public void testFive(){
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                Log.d(TAG, "emitter1");
                emitter.onNext(1);
                Log.d(TAG, "emitter2");
                emitter.onNext(2);
                Log.d(TAG, "emitter3");
                emitter.onNext(3);
                Log.d(TAG, "发送事件上游 " + Thread.currentThread().getName());
            }
        }).flatMap(new Function<Integer, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(Integer integer) throws Exception {//将发送的事件拆分，构造，新的事件发送
                final List<String> list = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    list.add("I am value " + integer);
                }
                Log.d(TAG, "拆分转化的线程 " + Thread.currentThread().getName());//线程在发送的事件线程中
                return Observable.fromIterable(list).delay(10, TimeUnit.MILLISECONDS);
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Log.d(TAG, "接收事件的线程 " + Thread.currentThread().getName());
                Log.d(TAG, s);
            }
        });
    }


    //zip操作符，将事件组合，再发送。。。需求：比如一个界面需要展示用户的一些信息, 而这些信息分别要从两个服务器接口中获取, 而只有当两个都获取到了之后才能进行展示, 这个时候就可以用Zip了
    public void testSix(){
        //两根水管同时开始发送, 每发送一个, Zip就组合一个, 再将组合结果发送给下游.
        Observable<String> observable1 = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                Log.d(TAG, "emit A");
                emitter.onNext("A");
//                Thread.sleep(1000);
                Log.d(TAG, "emit B");
                emitter.onNext("B");
//                Thread.sleep(1000);
                Log.d(TAG, "emit C");
                emitter.onNext("C");
//                Thread.sleep(1000);
                Log.d(TAG, "emit complete2");
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io());


       Observable<Integer> observable2 =  Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                Log.d(TAG, "emit 1");
                emitter.onNext(1);
               // Thread.sleep(1000);
                Log.d(TAG, "emit 2");
                emitter.onNext(2);
              //  Thread.sleep(1000);
                Log.d(TAG, "emit 3");
                emitter.onNext(3);
              //  Thread.sleep(1000);
                Log.d(TAG, "emit 4");
                emitter.onNext(4);
             //   Thread.sleep(1000);
                Log.d(TAG, "emit complete1");
                emitter.onComplete();

            }
        }).subscribeOn(Schedulers.newThread());


        Observable.zip(observable1, observable2, new BiFunction<String, Integer, String>() {//最后一个是返回得要求类型
            @Override
            public String apply(String s,Integer integer) throws Exception {//分别对应第一，第二个
                return integer + s;
            }
        }).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG, "onSubscribe");
            }

            @Override
            public void onNext(String value) {
                Log.d(TAG, "onNext: " + value);
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError"+e.getMessage());
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "onComplete");
            }
        });




    }


}
