package com.sbl.rxjavademo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG =  "songbl";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button1).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);
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
        }
    }


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


    //切断水管，链式操作，发送情况测试
    public void testTwo(){
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
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
                new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.i(TAG,"===="+integer);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.i(TAG, "error");
                    }
                }
        );

    }


    public void testThree(){

    }




}
