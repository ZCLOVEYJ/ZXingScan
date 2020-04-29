# ZXingScan

ZXingScan for Android 是ZXing的精简版，基于ZXing库优化扫码和生成二维码/条形码功能，扫码界面完全支持自定义，也可一行代码使用默认实现的扫码功能。总之你想要的都在这里。

## ViewfinderView属性说明
| 属性 | 值类型 | 默认值 | 说明 |
| :------| :------ | :------ | :------ |
| maskColor | color |<font color=#000000>#60000000</font>| 扫描区外遮罩的颜色 |
| frameColor | color |<font color=#1FB3E2>#7F1FB3E2</font>| 扫描区边框的颜色 |
| cornerColor | color |<font color=#1FB3E2>#FF1FB3E2</font>| 扫描区边角的颜色 |
| laserColor | color |<font color=#1FB3E2>#FF1FB3E2</font>| 扫描区激光线的颜色 |
| resultPointColor | color |<font color=#EFBD21>#C0EFBD21</font>| 扫描区结果点的颜色 |
| labelText | string |  | 扫描提示文本信息 |
| labelTextColor | color |<font color=#C0C0C0>#FFC0C0C0</font>| 提示文本字体颜色 |
| labelTextSize | dimension |14sp| 提示文本字体大小 |
| labelTextPadding | dimension |24dp| 提示文本距离扫描区的间距 |
| labelTextLocation | enum |bottom| 提示文本显示位置 |
| showResultPoint | boolean | false | 是否显示合适的扫码结果点 |
| frameWidth | dimension |  | 扫码框宽度 |
| frameHeight | dimension |  | 扫码框高度 |
| laserStyle | enum | line | 扫描激光的样式 |
| gridColumn | integer | 20 | 网格扫描激光列数 |
| gridHeight | integer | 40dp | 网格扫描激光高度，为0dp时，表示动态铺满 |
| cornerRectWidth | dimension | 4dp | 扫描区边角的宽 |
| cornerRectHeight | dimension | 16dp | 扫描区边角的高 |
| scannerLineMoveDistance | dimension | 2dp | 扫描线每次移动距离 |
| scannerLineHeight | dimension | 5dp | 扫描线高度 |
| frameLineWidth | dimension | 1dp | 边框线宽度 |
| scannerAnimationDelay | integer | 15 | 扫描动画延迟间隔时间，单位：毫秒 |
| frameRatio | float | 0.625f | 扫码框与屏幕占比 |


### 快速实现扫码有以下几种方式：

> 1、直接使用CaptureActivity或者CaptureFragment。(纯洁的扫码，无任何添加剂)

> 2、通过继承CaptureActivity或者CaptureFragment并自定义布局。（适用于大多场景，并无需关心扫码相关逻辑，自定义布局时需覆写getLayoutId方法）

> 3、在你项目的Activity或者Fragment中创建一个CaptureHelper并在相应的生命周期中调用CaptureHelper的周期。（适用于想在扫码界面写交互逻辑，又因为项目架构或其它原因，无法直接或间接继承CaptureActivity或CaptureFragment时使用）

> 4、参照CaptureHelper写一个自定义的扫码帮助类，其它步骤同方式3。（扩展高级用法，谨慎使用）

## 版本记录

*  优化一些默认参数配置
*  修复扫码界面开启闪光灯并切到后台时，手电筒按钮状态未同步问题（fix#81）
*  生成条形码/二维码时支持自定义配置颜色
*  支持识别反色码（增强识别率，默认不支持，需通过CaptureHelper.supportLuminanceInvert(true)开启）
*  优化Camera初始化相关策略，减少出现卡顿的可能性
*  内置手电筒按钮,当光线太暗时，自动显示手电筒 (fix#58)
*  生成二维码时Logo支持自定义大小 (fix#62)
*  支持真实识别区域比例和识别区域偏移量可配置
*  对外暴露更多可配置参数
*  支持扫二维码过小时，自动缩放
*  支持识别垂直条形码（增强条形码识别，默认不支持，需通过CaptureHelper.supportVerticalCode(true)开启）
*  将扫码相关逻辑与界面分离，ZXingLite使用更容易扩展
*  新增CaptureFragment
*  新增网格样式的扫描激光（类似支付宝扫码样式）
*  支持连续扫码
*  支持横屏扫码(主要为了支持Pad)
*  支持自定义扫码框宽高
*  修改text相关自定义属性，如：text->labelText
*  支持触摸缩放变焦
*  支持条形码下方显示显示code
*  优化相机预览尺寸遍历策略，从而降低预览变形的可能性
*  优化扫码识别速度






