# Android-data-binding

_**0. Installation**_

_gradle:_
```Gradle
dependencies {
    compile 'com.gplibs:data-binding:1.0.0'
}
```

<br />

---
_**1. JsonDataSource Binding**_

_json_data_source_binding_json.txt:_
```JavaScript
{
    name: "my name"
}
```

_StringJsonDataSourceBindingActivity:_
```Java
public class StringJsonDataSourceBindingActivity extends AppCompatActivity {

    @Binding(source = "name", target = "text")
    private TextView tvName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_string_json_data_source_binding);

        tvName = (TextView) findViewById(R.id.tv_name);

        String json = Utils.readText("json_data_source_binding_json.txt");
        BindingManager.binding(json, this);
    }
}
```

![image](https://github.com/gplibs/resources/raw/master/android/data-binding/readme/string_json_data_source_binding.png)

<br />

---
_**2. Convert Binding**_

_convert_binding_json.txt:_
```JavaScript
{
    name: "my name",
    sex: 1,
    head_url: "https://github.com/gplibs/resources/raw/master/sample.jpeg",
    is_vip: true
}
```

_BooleanToVisibilityConverter:_
```Java
class BooleanToVisibilityConverter implements IValueConverter {
    @Override
    public Object convert(Object sourceValue) {
        return ((Boolean) sourceValue) ? View.VISIBLE : View.GONE;
    }
}
```

_SexToStringConverter:_
```Java
class SexToStringConverter implements IValueConverter {
    @Override
    public Object convert(Object sourceValue) {
        return (((Integer) sourceValue) == 0) ? "Female" : "Male";
    }
}
```

_UrlToBitmapConverter:(implements **IAsyncValueConverter**)_
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

    @ConvertBinding(source = "sex", target = "text", converter = SexToStringConverter.class)
    private TextView tvSex;

    @ConvertBinding(source = "is_vip", target = "visibility", converter = BooleanToVisibilityConverter.class)
    private TextView tvVip;

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

        String json = Utils.readText("convert_binding_json.txt");
        BindingManager.binding(json, this);
    }

}
```

![image](https://github.com/gplibs/resources/raw/master/android/data-binding/readme/convert_binding.png)

<br />

---
_**3. Multi Binding**_

_multi_binding_json.txt:_
```JavaScript
{
    name: "my name",
    is_vip: true,
    vip_data: "I am Vip"
}
```

_BooleanToVisibilityConverter:_
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

    @ConvertBinding(source = "is_vip", target = "visibility", converter = BooleanToVisibilityConverter.class)
    @Binding(source = "vip_data", target = "text")
    private TextView tvVip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_binding);

        tvName = (TextView) findViewById(R.id.tv_name);
        tvVip = (TextView) findViewById(R.id.tv_vip);

        String json = Utils.readText("multi_binding_json.txt");
        BindingManager.binding(json, this);
    }

}
```

![image](https://github.com/gplibs/resources/raw/master/android/data-binding/readme/multi_binding.png)

<br />

---
_**4. Path Binding**_

_path_binding_json.txt:_
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

    @Binding(source = "father.name", target = "text")
    private TextView tvFatherName;

    @Binding(source = "children.[0].name", target = "text")
    private TextView tvSonName;

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

        String json = Utils.readText("path_binding_json.txt");
        BindingManager.binding(json, this);
    }

}
```

![image](https://github.com/gplibs/resources/raw/master/android/data-binding/readme/path_binding.png)

<br />

---
_**5. Other DataSource**_

_a. ModelSource:_

```Java
class TestModel extends ModelSource {

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
                testModel.setProperty("name", "new name " + (++index));
            }
        });

        BindingManager.binding(testModel, this);
    }

}
```

![image](https://github.com/gplibs/resources/raw/master/android/data-binding/readme/model_source_0.png) ![image](https://github.com/gplibs/resources/raw/master/android/data-binding/readme/model_source_1.png)

_b. ArraySource:_

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

_c. CollectionSource:_

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

_d. MapSource:_

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
_**6. Mix DataSource**_

_mix_binding_json.txt:_
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
_**7. Custom DataSource**_

```Java
class UserNameAdapter implements IDataSourceAdapter<String> {

    @Override
    public IDataSource getDataSource(String data) {
        if (TextUtils.isEmpty(data) || !data.matches("^[a-zA-Z]+\\s+[a-zA-Z]+$")) {
            return null;
        }
        final String[] a = data.split("\\s+");
        return new IDataSource() {
            @Override
            public Object getProperty(String propertyName) {
                if ("firstName".equals(propertyName)) {
                    return a[0];
                }
                if ("lastName".equals(propertyName)) {
                    return a[1];
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
_**8. Notes**_

@Binding(source = "..", **target** = "text")

xx.set**Text**(text)  target is "**text**"

xx.set**Visibility**(visibility)  target is "**visibility**"

xx.set**BackgroundColor**(color)  target is "**backgroundColor**"

...

and so on