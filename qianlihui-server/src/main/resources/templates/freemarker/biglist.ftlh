<!DOCTYPE html>

<html>

<head>
    <title>Thymeleaf Sandbox</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <link rel="stylesheet" type="text/css" href="../../static/layui/css/layui.css">
<#--    <script src="../../static/layui/layui.js" />-->
</head>

<body>

<h2>Big Listing (8,715 * 300 = 2,614,500 entries)</h2>

<p>Using template engine: <strong>FreeMarker</strong></p>

<table>
    <thead>
    <tr>
        <th>Playlist ID</th>
        <th>Playlist Name</th>
        <th>Track Name</th>
        <th>Artist Name</th>
        <th>Album Title</th>
    </tr>
    </thead>
    <tbody>
    <#list dataSource as e>
        <tr>
            <td>${e.playlistId}</td>
            <td>${e.playlistName}</td>
            <td>${e.trackName}</td>
            <td>${e.artistName}</td>
            <td>${e.albumTitle}</td>
        </tr>
    </#list>
    </tbody>
</table>

<p>默认风格:</p>
<table class="layui-table">
    <colgroup>
        <col width="150">
        <col width="150">
        <col>
    </colgroup>
    <thead>
    <tr>
        <th>人物</th>
        <th>民族</th>
        <th>格言</th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td>孔子</td>
        <td>华夏</td>
        <td>有朋自远方来，不亦乐乎</td>
    </tr>
    <tr>
        <td>孟子</td>
        <td>华夏</td>
        <td>穷则独善其身，达则兼济天下</td>
    </tr>
    </tbody>
</table>
<p>行边框表格:</p>
<table class="layui-table" lay-skin="line">
    <colgroup>
        <col width="150">
        <col width="150">
        <col>
    </colgroup>
    <thead>
    <tr>
        <th>人物</th>
        <th>民族</th>
        <th>格言</th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td>孔子</td>
        <td>华夏</td>
        <td>有朋自远方来，不亦乐乎</td>
    </tr>
    <tr>
        <td>孟子</td>
        <td>华夏</td>
        <td>穷则独善其身，达则兼济天下</td>
    </tr>
    </tbody>
</table>
<p>列边框表格:</p>
<table class="layui-table" lay-skin="row">
    <colgroup>
        <col width="150">
        <col width="150">
        <col>
    </colgroup>
    <thead>
    <tr>
        <th>人物</th>
        <th>民族</th>
        <th>格言</th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td>孔子</td>
        <td>华夏</td>
        <td>有朋自远方来，不亦乐乎</td>
    </tr>
    <tr>
        <td>孟子</td>
        <td>华夏</td>
        <td>穷则独善其身，达则兼济天下</td>
    </tr>
    </tbody>
</table>
<p>无边框表格:</p>
<table class="layui-table" lay-skin="nob">
    <colgroup>
        <col width="150">
        <col width="150">
        <col>
    </colgroup>
    <thead>
    <tr>
        <th>人物</th>
        <th>民族</th>
        <th>格言</th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td>孔子</td>
        <td>华夏</td>
        <td>有朋自远方来，不亦乐乎</td>
    </tr>
    <tr>
        <td>孟子</td>
        <td>华夏</td>
        <td>穷则独善其身，达则兼济天下</td>
    </tr>
    </tbody>
</table>
<p>小尺寸表格:</p>
<table class="layui-table" lay-size="sm">
    <colgroup>
        <col width="150">
        <col width="150">
        <col>
    </colgroup>
    <thead>
    <tr>
        <th>人物</th>
        <th>民族</th>
        <th>格言</th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td>孔子</td>
        <td>华夏</td>
        <td>有朋自远方来，不亦乐乎</td>
    </tr>
    <tr>
        <td>孟子</td>
        <td>华夏</td>
        <td>穷则独善其身，达则兼济天下</td>
    </tr>
    </tbody>
</table>
<p>大尺寸表格:</p>
<table class="layui-table" lay-size="lg">
    <colgroup>
        <col width="150">
        <col width="150">
        <col>
    </colgroup>
    <thead>
    <tr>
        <th>人物</th>
        <th>民族</th>
        <th>格言</th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td>孔子</td>
        <td>华夏</td>
        <td>有朋自远方来，不亦乐乎</td>
    </tr>
    <tr>
        <td>孟子</td>
        <td>华夏</td>
        <td>穷则独善其身，达则兼济天下</td>
    </tr>
    </tbody>
</table>
<p>开启偶数行背景色:</p>
<table class="layui-table" lay-even>
    <colgroup>
        <col width="150">
        <col width="150">
        <col>
    </colgroup>
    <thead>
    <tr>
        <th>人物</th>
        <th>民族</th>
        <th>格言</th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td>孔子</td>
        <td>华夏</td>
        <td>有朋自远方来，不亦乐乎</td>
    </tr>
    <tr>
        <td>孟子</td>
        <td>华夏</td>
        <td>穷则独善其身，达则兼济天下</td>
    </tr>
    <tr>
        <td>庄子</td>
        <td>华夏</td>
        <td>朴素而天下莫能与之争美</td>
    </tr>
    </tbody>
</table>


</body>
</html>