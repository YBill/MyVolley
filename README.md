# MyVolley
自己实现的Volley，具体实现是按照Volley的流程，不过特别简单，能正常请求http请求。         主要是理清逻辑，看下谷歌工程师是怎样编码的。

- **Network** 接口，是用来请求网络数据的，传入我们的请求参数 Request，返回响应结果 NetworkResponse
- **BasicNetwork** Network的实现类，需要传入一个 HttpStack 参数，然后创建一个 AdaptedHttpStack 类 ，并将 HttpStack 参数传给他，通过 AdaptedHttpStack 分发具体的网络请求。并将 AdaptedHttpStack 执行请求的 HttpResponse 对象包装为 NetworkResponse 对象。
- **HttpStack** 接口，真正的网络请求，定义接口 performRequest，传入我们的请求包装类Request和附加头信息，并返回网络响应对象   org.apache.http.HttpResponse类
- **BaseHttpStack** 抽象类，定义抽象方法 executeRequest，实现了 HttpStack 接口，感觉这个写的有点绕，其实是调用 HurlStack 的 performRequest 方法然后 performRequest 方法在 BaseHttpStack 内完成，在 BaseHttpStack 内又会执行 executeRequest 方法还是在 HurlStack 中完成。
- **HurlStack** 实现 HttpStack 接口，真正的执行网络请求，内部通过 HttpURLConnection 实现网络请求
- **HttpClientStack** 实现 HttpStack 接口，真正的执行网络请求，内部通过 HttpClient 实现网络请求
- **AdaptedHttpStack** 网络请求适配器，构造函数需要传入一个 HttpStack 参数用来真正执行网络请求，实际传入的就是 HurlStack 和 HttpClientStack ，将网络响应对象 org.apache.http.HttpResponse 包装成 Volley 内部自己封装的 HttpResponse 对象。
- **Cache** 接口，定义了缓存的基本方法，自己可以实现缓存策略，继承 Cache 即可。
- **DiskBasedCache** 继承了 Cache 接口，缓存的具体实现类。
- **CacheDispatcher** 继承 Thread，在 run() 中从缓存队列中取出 Request 请求，没有过期则返回给调用者，否则加入网络请求队列。
- **NetworkDispatcher** 继承 Thread，在 run() 中通过 BasicNetwork 请求网络，将返回结果添加到缓存队列，并返回给调用者。
- **ResponseDelivery** 接口，定义接口，将服务器响应交付到调用者。
- **ExecutorDelivery** 继承 ResponseDelivery，将服务器响应错误或正确的响应通过 Handler 分发到主线程，并交付到调用者。
- **Request** 抽象类，网络请求的基类，里面包含网络请求的所有信息（网络地址，请求体，请求头，调用者回调对象等等），交付到调用者的错误交付就在这里完成，方法是公有的子类也可复写，成功的交付需有子类实现 deliverResponse 抽象方法完成。
- **StringRequest** 继承 Request，实现 deliverResponse 抽象方法，将网络请求返回给调用者，实现 parseNetworkResponse 抽象方法，将网络返回的 NetworkResponse  解析为 String 类型，并包装为 Response 对象。
- **Response** 内部定义了与调用者交互的成功与错误接口，封装了网络请求成功与失败的数据。
- **RequestQueue** 包含了缓存队列(PriorityBlockingQueue)、网络请求队列(PriorityBlockingQueue)、缓存(Cache)、网络请求(Network)、交付(ResponseDelivery)、执行缓存的线程(CacheDispatcher)和4个执行网络请求的线程(NetworkDispatcher)，都是接口的形式，可以配置实现形式。
- **Volley** 提供创建 RequestQueue 并启动，创建缓存 DiskBasedCache，创建 BasicNetwork，当SDK版本大于等于9 创建 HurlStack，否则创建 HttpClientStack。

> 整理流程是在 RequestQueue 中启动一个缓存线程(CacheDispatcher)和4个网络线程(NetworkDispatcher)，并阻塞等待请求到来，在 RequestQueue 中调用 add 添加请求，请求到来后会有一定的策略看是重缓存中获取还是通过网络请求数据，在网络线程中是通过 BasicNetwork 获取网络数据，BasicNetwork 中又通过 AdaptedHttpStack 来分发转换，SDK 小于9就使用 HttpClientStack，否则使用 HurlStack 来请求网络，最后通过 ExecutorDelivery 将结果返回。
>
> 可以看到源码中所有地方都是传入的接口，扩展性比较强。

#### 大体步骤：

```java
                RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = "http://www.mocky.io/v2/5ce7a43d3500008d00cf6014";
        StringRequest request = new StringRequest(
                url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d("Bill", response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Bill", "error:" + error.getMessage());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return super.getParams();
            }
        };
        requestQueue.add(request);
```

##### 1、Volley.newRequestQueue 得到一个 RequestQueue：会判断 SDK 版本大于等于9创建一个 HurlStack，小于9创建一个 HttpClientStack，然后创建一个 BasicNetwork，并将刚创建的 HttpStack 传进去，然后创建 DiskBasedCache，再创建 RequestQueue，并将Network 和 Cache传入，并调用 RequestQueue的 start() 方法，在 start() 中执行缓存线程 CacheDispatcher 和4个网络线程 NetworkDispatcher，内部 run() 中都启动了死循环，并从各自的队列中阻塞取出，没有 Request 则一直阻塞，有 Request 到来则执行后面请求。

##### 2、new StringRequest() 得到一个Request：将网络请求的url，请求类型、成功与失败回到传入，如果请求类型是 post 请求的话 HurlStack或 HttpClientStack 会从 Request 获取getParams() 参数。

##### 3、requestQueue.add(request) ：通过1得到的 RequestQueue，调用其 add() 方法，在 add() 中，如果不需要缓存则加入网络请求队列中，需要缓存则加入缓存队列中，默认是需要缓存的。

##### 4、上面调用代码就完了，就等待回调了，看下后面真正的执行，通过上面请求会加入到缓存队列，在 CacheDispatcher 的 run() 中缓存队列中获取到 Request 后，有缓存并且没有过期则取出解析并通过 ResponseDelivery 返回给调用端，否则添加到网络缓存队列中。在 NetworkDispatcher 中会通过 BasicNetwork 请求网络，在 BasicNetwork 中会创建一个 AdaptedHttpStack，并将1中创建的 HttpStack 传入，由 AdaptedHttpStack去分发网络请求，请求成功返回 NetworkResponse 对象，再通过 Request 的子类 parseNetworkResponse() 方法解析为自己所需要的数据得到Response对象(比如StringRequest 解析为String并赋值到Response中)，然后需要缓存的话在此缓存到 Cache 中，最后通过 ResponseDelivery 交付到调用端。ExecutorDelivery 是在 RequestQueue 的构造函数中创建的，并使用主线程的 Looper将网络线程切换到主线程，并将调用端需要的类型交付。
