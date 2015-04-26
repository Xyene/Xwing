Xwing
=====

XML Swing layout engine - XML Windowing Toolkit. Xwing takes a JavaScript-enabled HTML-like representation of your layout and automagically generates your desired window.

For example, a simple calculator layout, as shown below, is easy to make with Xwing.

![!!!](https://sc-cdn.scaleengine.net/i/535f69370dcfc771fe9a231ef6d9cdeb.png)

```html
<window title="Calculator" id="main-window" resizable="true" width="325" height="310">
    <script>
        var buttons = $("commands").getComponents();
        buttons.forEach(function(button) {
            button.onActionPerformed(function(evt) {
                var btn = button.getText();
                var display = $("display");
                if(btn == 'CE') {
                    display.setText('0');
                } else if(btn == 'DEL') {
                    display.setText(display.getText().substring(1));
                } else if(btn == '=') {
                    display.setText('' + eval(display.getText()));
                } else {
                    var text = display.getText();
                    display.setText(text == '0' ? btn : (text + btn));
                }
            });
        });
    </script>
    <menubar>
        <menu text="Settings"/>
    </menubar>
    <panel name="Calculator">
        <vbox position="north">
            <textbox id="display" text="0" height="50"/>
            <hr/>
        </vbox>
        <grid position="center" id="commands" rows="4" columns="6">
            <button text="1"/><button text="2"/><button text="3"/>
            <button text="/"/><button text="("/> <button text=")"/>
            <button text="4"/><button text="5"/><button text="6"/>
            <button text="*"/><button text="DEL"/><button text="CE"/>
            <button text="7"/><button text="8"/><button text="9"/>
            <button text="-"/><button text="="/><button text="0"/>
            <button text="."/><button text="%"/><button text="+"/>
        </grid>
    </panel>
</window>
```

Simply load the resource and go!

```java
XFrame fr = new XFrame(new FileInputStream("calc.xw"), new Object());
fr.setVisible(true);
```
