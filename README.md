# android-data-binding

这是一个可以将 json 字符串 直接绑定到 view 上的库， 不用先将 json 转换为 model 类。

_**1. 安装**_

_gradle:_
```Gradle
dependencies {
    compile 'com.gplibs:data-binding:1.0.0'
}
```

<br />

---
_**2. 一个简单的例子**_

_json字符串数据源 json_data_source_binding_json.txt:_
```JavaScript
{
    name: "my name"
}
```

_StringJsonDataSourceBindingActivity:_
```Java
public class StringJsonDataSourceBindingActivity extends AppCompatActivity {

    // 将 json 中的 name 字符段绑定到 TextView 的 text 上
    @Binding(source = "name", target = "text")
    private TextView tvName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_string_json_data_source_binding);

        tvName = (TextView) findViewById(R.id.tv_name);

        // 读取json数据
        String json = Utils.readText("json_data_source_binding_json.txt");
        // 绑定操作
        BindingManager.binding(json, this);
    }
}
```

运行结果

![image](https://github.com/gplibs/resources/raw/master/android/data-binding/readme/string_json_data_source_binding.png)

<br />

---
_**3. 值转换**_

某些时候数据源的类型可能与 view 的目标字段类型不一致，此时就需要对值进行转换。

_json字符串数据源 convert_binding_json.txt:_
```JavaScript
{
    name: "my name",
    sex: 1,
    head_url: "https://github.com/gplibs/resources/raw/master/sample.jpeg",
    is_vip: true
}
```

_定义一个将 boolean 转换为 visibility 的转换器:_
```Java
class BooleanToVisibilityConverter implements IValueConverter {
    @Override
    public Object convert(Object sourceValue) {
        return ((Boolean) sourceValue) ? View.VISIBLE : View.GONE;
    }
}
```

_定义一个将 表示性别的整形值 转换为对应文本的转换器:_
```Java
class SexToStringConverter implements IValueConverter {
    @Override
    public Object convert(Object sourceValue) {
        return (((Integer) sourceValue) == 0) ? "Female" : "Male";
    }
}
```

_定义一个将 url字符串 转换为 bitmap 的转换器:_

(为了不阻塞主线程,实现的是一个异步转换器接口 **IAsyncValueConverter**)

```Java
class UrlToBitmapConverter implements IAsyncValueConverter {
    @Override
    public void convert(final Object sourceValue, final IValueConverterCallback callback) {
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL((String) sourceValue);
                    callback.run(BitmapFactory.decodeStream(url.openStream()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}
```

_ConvertBindingActivity:_
```Java
public class ConvertBindingActivity extends AppCompatActivity {

    @Binding(source = "name", target = "text")
    private TextView tvName;

    // 使用转换器将 json 中整形字段 sex 转换为对应文案， 绑定到 TextView 的 text 上
    @ConvertBinding(source = "sex", target = "text", converter = SexToStringConverter.class)
    private TextView tvSex;

    // 使用转换器将 json 中布尔字段 is_vip 转换为对应 visibility， 绑定到 TextView 的 visibility 上, 是 vip 才显示
    @ConvertBinding(source = "is_vip", target = "visibility", converter = BooleanToVisibilityConverter.class)
    private TextView tvVip;

    // 使用转换器将 json 中布尔字段 head_url 转换为 bitmap， 绑定到 ImageView 的 imageBitmap 上
    @ConvertBinding(source = "head_url", target = "imageBitmap", converter = UrlToBitmapConverter.class)
    private ImageView ivHead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_convert_binding);

        tvName = (TextView) findViewById(R.id.tv_name);
        tvSex = (TextView) findViewById(R.id.tv_sex);
        tvVip = (TextView) findViewById(R.id.tv_vip);
        ivHead = (ImageView) findViewById(R.id.iv_head);

        // 读取json数据
        String json = Utils.readText("convert_binding_json.txt");
        // 绑定操作
        BindingManager.binding(json, this);
    }

}
```

运行结果

![image](https://github.com/gplibs/resources/raw/master/android/data-binding/readme/convert_binding.png)

<br />

---
_**4. 将多个字段绑定到同一个 View 的不同属性上**_

_json字符串数据源 multi_binding_json.txt:_
```JavaScript
{
    name: "my name",
    is_vip: true,
    vip_data: "I am Vip"
}
```

_定义一个将 boolean 转换为 visibility 的转换器:_
```Java
class BooleanToVisibilityConverter implements IValueConverter {
    @Override
    public Object convert(Object sourceValue) {
        return ((Boolean) sourceValue) ? View.VISIBLE : View.GONE;
    }
}
```

_MultiBindingActivity:_
```Java
public class MultiBindingActivity extends AppCompatActivity {

    @Binding(source = "name", target = "text")
    private TextView tvName;

    // 将 json 字段 is_vip 绑定到 TextView 的 visibility 上, 是 vip 才显示
    // 将 json 字段 vip_data 绑定到 TextView 的 text 上
    @ConvertBinding(source = "is_vip", target = "visibility", converter = BooleanToVisibilityConverter.class)
    @Binding(source = "vip_data", target = "text")
    private TextView tvVip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_binding);

        tvName = (TextView) findViewById(R.id.tv_name);
        tvVip = (TextView) findViewById(R.id.tv_vip);

        // 读取 json 文本
        String json = Utils.readText("multi_binding_json.txt");
        // 绑定操作
        BindingManager.binding(json, this);
    }

}
```

运行结果

![image](https://github.com/gplibs/resources/raw/master/android/data-binding/readme/multi_binding.png)

<br />

---
_**5. 路径绑定**_

某些 json 有较复杂的数据结构，有子对象 或者 数组; 我们也可以将子对象或者数组中的字段绑定到 View 上;

路径绑定语法中 "." 可以获取子对象; ".[数字索引]" 可以获取数组中某索引处的元素。

_json字符串数据源 path_binding_json.txt:_
```JavaScript
{
    name: "my name",
    father:
    {
        name: "my father"
    },
    children:
    [
        {
            name: "my son"
        },
        {
            name: "my daughter"
        }
    ]
}
```

_PathBindingActivity:_
```Java
public class PathBindingActivity extends AppCompatActivity {

    @Binding(source = "name", target = "text")
    private TextView tvName;

    // 将 json 中子对象 father 的 name 字段绑定到 TextView 的 text 上
    @Binding(source = "father.name", target = "text")
    private TextView tvFatherName;

    // 将 json 中子对象 children 数组的第0个元素的 name 字段绑定到 TextView 的 text 上
    @Binding(source = "children.[0].name", target = "text")
    private TextView tvSonName;

    // 将 json 中子对象 children 数组的第1个元素的 name 字段绑定到 TextView 的 text 上
    @Binding(source = "children.[1].name", target = "text")
    private TextView tvDaughterName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path_binding);

        tvName = (TextView) findViewById(R.id.tv_name);
        tvFatherName = (TextView) findViewById(R.id.tv_father_name);
        tvSonName = (TextView) findViewById(R.id.tv_son_name);
        tvDaughterName = (TextView) findViewById(R.id.tv_daughter_name);

        // 读取 json 文本
        String json = Utils.readText("path_binding_json.txt");
        // 绑定操作
        BindingManager.binding(json, this);
    }

}
```

运行结果

![image](https://github.com/gplibs/resources/raw/master/android/data-binding/readme/path_binding.png)

<br />

---
_**5. 其他非字符串数据源**_

除了可以使用 json 字符串作为数据源外，我们也简单支持其他数据源进行绑定。

_a. Model类作为数据源， 需实现 ModelSource:_

```Java
class TestModel extends ModelSource {

    // @BindingField注解作用为：让框架使用其标注的名称作为绑定中数据源字段名
    // GSON 中的 @SerializedName 注解也有同样的效果
    @BindingField("name")
    public String name = "my name";

}
```

```Java
public class ModelSourceActivity extends AppCompatActivity {

    @Binding(source = "name", target = "text")
    private TextView tvName;

    private Button btnTest;

    private int index;

    private TestModel testModel = new TestModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model_source);

        tvName = (TextView) findViewById(R.id.tv_name);
        btnTest = (Button) findViewById(R.id.btn_test);

        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 修改 ModelSource 的 name 属性, 界面也会一起改变。
                testModel.setProperty("name", "new name " + (++index));
            }
        });

        // 绑定操作
        BindingManager.binding(testModel, this);
    }

}
```

运行结果

![image](https://github.com/gplibs/resources/raw/master/android/data-binding/readme/model_source_0.png) ![image](https://github.com/gplibs/resources/raw/master/android/data-binding/readme/model_source_1.png)

_b. 数组数据源:_

可以使用 "[数字索引]" 语法将某个元素绑定到 View。

```Java
public class ArraySourceActivity extends AppCompatActivity {

    @Binding(source = "[0]", target = "text")
    private TextView tvName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_array_source);

        tvName = (TextView) findViewById(R.id.tv_name);

        String[] data = new String[] { "item1", "item2", "item3" };
        BindingManager.binding(data, this);
    }

}
```

_c. Collection数据源:_

与数组数据源一样

```Java
public class CollectionSourceActivity extends AppCompatActivity {

    @Binding(source = "[1]", target = "text")
    private TextView tvName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collection_source);

        tvName = (TextView) findViewById(R.id.tv_name);

        Collection<String> data = new ArrayList<>();
        data.add("item1");
        data.add("item2");
        data.add("item3");
        BindingManager.binding(data, this);
    }

}
```

_d. Map数据源:_

只支持 Key 类型为 String 的 Map。

```Java
public class MapSourceActivity extends AppCompatActivity {

    @Binding(source = "key1", target = "text")
    private TextView tvName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_source);

        tvName = (TextView) findViewById(R.id.tv_name);

        Map<String, String> data = new HashMap<>();
        data.put("key1", "value1");
        BindingManager.binding(data, this);
    }

}
```

<br />

---
_**6. 各种数据混合时，可以按路径语法绑定指定字段到 View 上**_

_json字符串数据源 mix_binding_json.txt:_
```JavaScript
{
    name: "my name"
}
```

```Java
class MixTestModel extends ModelSource {

    @BindingField("data")
    public Map<String, Collection<String>> data;

    public MixTestModel() {
        data = new HashMap<>();
        Collection<String> c = new ArrayList<>();
        c.add(Utils.readText("mix_binding_json.txt"));
        data.put("my_collection", c);
    }

}
```

```Java
public class MixDataSourceBindingActivity extends AppCompatActivity {

    // 将 MixTestModel 的Map数据源字段 "data" 中key为 "my_collection" 的Collection值 中的第 "0" 个json字符串元素 中的 "name" 字段 绑定到 View
    @Binding(source = "data.my_collection.[0].name", target = "text")
    private TextView tvName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mix_data_source_binding);

        tvName = (TextView) findViewById(R.id.tv_name);

        MixTestModel model = new MixTestModel();
        BindingManager.binding(model, this);
    }

}
```

<br />

---
_**7. 自定义数据源**_

以下例子为一个符合英文名格式的字符串 转换为 有 "firstName" 和 "lastName" 属性的数据源。

```Java
class UserNameAdapter implements IDataSourceAdapter<String> {

    @Override
    public IDataSource getDataSource(String data) {
        if (TextUtils.isEmpty(data) || !data.matches("^([a-zA-Z]+\\s)+[a-zA-Z]+$")) {
            return null;
        }
        int i = data.indexOf(" ");
        String f = data.substring(0, i);
        String l = data.substring(i + 1);
        return new IDataSource() {
            @Override
            public Object getProperty(String propertyName) {
                if ("firstName".equals(propertyName)) {
                    return f;
                }
                if ("lastName".equals(propertyName)) {
                    return l;
                }
                return "";
            }
        };
    }

    @Override
    public Type typeOfData() {
        return String.class;
    }
}
```

```Java
public class CustomDataSourceAdapterActivity extends AppCompatActivity {

    @Binding(source = "firstName", target = "text")
    private TextView tvFirstName;

    @Binding(source = "lastName", target = "text")
    private TextView tvLastName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_data_source_adapter);

        BindingManager.registerDataSourceAdapter(new UserNameAdapter());

        tvFirstName = (TextView) findViewById(R.id.tv_first_name);
        tvLastName = (TextView) findViewById(R.id.tv_last_name);

        String data = "William Shakespeare";
        BindingManager.binding(data, this);
    }

}
```

<br />

---
_**8. 备注**_

@Binding(source = "..", **target** = "text")

绑定时 target 即绑定到 View 的目标属性， 取值按如下规律：

xx.set**Text**(text) | target = "**text**"

xx.set**Visibility**(visibility) | target = "**visibility**"

xx.set**BackgroundColor**(color) | target = "**backgroundColor**"

...

以此类推