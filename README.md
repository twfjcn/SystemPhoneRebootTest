# ProxyRouteGuard
Android root 路由守护App，完全复刻 proxy.sh 脚本功能。
Kotlin原生实现，不依赖外部sh脚本，前台服务守护路由策略，UI提供启动/停止按钮。

> 原脚本功能：ip转发、iptables NAT、ip‑rule路由策略，定时检测丢失自动恢复；停止时自动清理路由规则。

## ⚠️ 前置条件
1. Android设备必须 **Root**
2. 建议临时关闭SELinux：`su setenforce 0`
3. tun0 虚拟接口由VPN程序预先创建，本App**不会创建tun0**
4. 默认物理网卡 `wlan0`，代码内可修改为 `eth0`

## 参数配置
`RouteGuardService.kt` 顶部常量：
```kotlin
private val tun = "tun0"
private val dev = "wlan0"
private val intervalMs = 3000L
private val pref = 18000
private val prefMinus1 = pref - 1
