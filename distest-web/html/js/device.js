/**
 * Created by wen on 2017/4/24.
 */


var util = {
    isRotate: false
};
var webSocket = null;

String.prototype.startWith=function(str){
    var reg=new RegExp("^"+str);
    return reg.test(this);
};

String.prototype.endWith=function(str){
    var reg=new RegExp(str+"$");
    return reg.test(this);
};

var canvas = document.getElementById("phone-screen");
var g = canvas.getContext('2d');


document.onkeydown = function (event) {
    var e = event || window.event || arguments.callee.caller.arguments[0];
    console.log(e.keyCode);
    sendKeyEvent(e.keyCode);
};

function setCanvasImageData(data) {
    var blob = new Blob([data], {type: 'image/jpeg'});
    var URL = window.URL || window.webkitURL;
    var img = new Image();
    img.onload = function () {
        canvas.width = img.width;
        canvas.height = img.height;
        g.drawImage(img, 0, 0);
        img.onLoad = null;
        img = null;
        u = null;
        blob = null;
    };
    var u = URL.createObjectURL(blob);
    img.src = u;
}

// 获取鼠标在html中的绝对位置
function mouseCoords(event){
    if(event.pageX || event.pageY){
        return {x:event.pageX, y:event.pageY};
    }
    return{
        x:event.clientX + document.body.scrollLeft - document.body.clientLeft,
        y:event.clientY + document.body.scrollTop - document.body.clientTop
    };
}
// 获取鼠标在控件的相对位置
function getXAndY(control, event){
    //鼠标点击的绝对位置
    Ev= event || window.event;
    var mousePos = mouseCoords(event);
    var x = mousePos.x;
    var y = mousePos.y;
    //alert("鼠标点击的绝对位置坐标："+x+","+y);

    //获取div在body中的绝对位置
    var x1 = control.offsetLeft;
    var y1 = control.offsetTop;

    //鼠标点击位置相对于div的坐标
    var x2 = x - x1;
    var y2 = y - y1;
    return {x:x2,y:y2};
}

var isDown = false;


function textInput(str) {
    webSocket.send("input://" + $("#text-input").val());
}

function sendTouchEvent(minitouchStr) {
    webSocket.send("touch://" + minitouchStr);
}

function sendKeyEvent(keyevent) {
    webSocket.send("keyevent://" + convertAndroidKeyCode(keyevent));
}

