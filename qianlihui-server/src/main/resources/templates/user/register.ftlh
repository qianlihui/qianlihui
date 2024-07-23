<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Demo</title>
    <!-- 请勿在项目正式环境中引用该 layui.css 地址 -->
    <link href="//unpkg.com/layui@2.9.14/dist/css/layui.css" rel="stylesheet">
</head>
<body>
<style>
    .demo-reg-container{width: 320px; margin: 21px auto 0;}
    .demo-reg-other .layui-icon{position: relative; display: inline-block; margin: 0 2px; top: 2px; font-size: 26px;}
</style>
<form class="layui-form" action="/user/register" method="post">
    <div class="demo-reg-container">
        <div class="layui-form-item">
            <div class="layui-row">
                <div class="layui-col-xs7">
                    <div class="layui-input-wrap">
                        <div class="layui-input-prefix">
                            <i class="layui-icon layui-icon-cellphone"></i>
                        </div>
                        <input type="text" name="phone" value="" lay-verify="required|phone" placeholder="手机号" lay-reqtext="请填写手机号" autocomplete="off" class="layui-input" id="reg-cellphone">
                    </div>
                </div>
<#--                <div class="layui-col-xs5">-->
<#--                    <div style="margin-left: 11px;">-->
<#--                        <button type="button" class="layui-btn layui-btn-fluid layui-btn-primary" lay-on="reg-get-vercode">获取验证码</button>-->
<#--                    </div>-->
<#--                </div>-->
            </div>
        </div>
        <div class="layui-form-item">
            <div class="layui-input-wrap">
                <div class="layui-input-prefix">
                    <i class="layui-icon layui-icon-vercode"></i>
                </div>
                <input type="text" name="name" value="" lay-verify="required" placeholder="名称" lay-reqtext="请填写名称" autocomplete="off" class="layui-input">
            </div>
        </div>
        <div class="layui-form-item">
            <div class="layui-input-wrap">
                <div class="layui-input-prefix">
                    <i class="layui-icon layui-icon-password"></i>
                </div>
                <input type="password" name="password" value="" lay-verify="required" placeholder="密码" autocomplete="off" class="layui-input" id="reg-password" lay-affix="eye">
            </div>
        </div>
        <div class="layui-form-item">
            <div class="layui-input-wrap">
                <div class="layui-input-prefix">
                    <i class="layui-icon layui-icon-password"></i>
                </div>
                <input type="password" name="confirmPassword" value="" lay-verify="required|confirmPassword" placeholder="确认密码" autocomplete="off" class="layui-input" lay-affix="eye">
            </div>
        </div>
        <div class="layui-form-item">
            <div class="layui-input-wrap">
                <div class="layui-input-prefix">
                    <i class="layui-icon layui-icon-username"></i>
                </div>
                <input type="text" name="nickname" value="" lay-verify="required" placeholder="昵称" autocomplete="off" class="layui-input" lay-affix="clear">
            </div>
        </div>
        <div class="layui-form-item">
            <input type="checkbox" name="agreement" lay-verify="required" lay-skin="primary" title="同意">
            <a href="#terms" target="_blank" style="position: relative; top: 6px; left: -15px;">
                <ins>用户协议</ins>
            </a>
        </div>
        <button class="layui-btn layui-btn-fluid" >注册</button>
        <div class="layui-form-item demo-reg-other">
            <a href="/user/login">登录已有帐号</a>
        </div>
    </div>
</form>

<!-- 请勿在项目正式环境中引用该 layui.js 地址 -->
<script src="//unpkg.com/layui@2.9.14/dist/layui.js"></script>
<script>
    layui.use(function(){
        var $ = layui.$;
        var form = layui.form;
        var layer = layui.layer;
        var util = layui.util;

        // 自定义验证规则
        form.verify({
            // 确认密码
            confirmPassword: function(value, item){
                var passwordValue = $('#reg-password').val();
                if(value !== passwordValue){
                    return '两次密码输入不一致';
                }
            }
        });

        // 提交事件
        form.on('submit(demo-reg)', function(data){
            var field = data.field; // 获取表单字段值

            // 是否勾选同意
            if(!field.agreement){
                layer.msg('您必须勾选同意用户协议才能注册');
                return false;
            }

            // 显示填写结果，仅作演示用
            layer.alert(JSON.stringify(field), {
                title: '当前填写的字段值'
            });

            // 此处可执行 Ajax 等操作
            // …

            return false; // 阻止默认 form 跳转
        });

        // 普通事件
        util.on('lay-on', {
            // 获取验证码
            'reg-get-vercode': function(othis){
                var isvalid = form.validate('#reg-cellphone'); // 主动触发验证，v2.7.0 新增
                // 验证通过
                if(isvalid){
                    layer.msg('手机号规则验证通过');
                    // 此处可继续书写「发送验证码」等后续逻辑
                    // …
                }
            }
        });
    });
</script>

</body>
</html>