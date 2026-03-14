# NeoGrid
Modern, automated electrical grids for NeoForge.

## Features
- **Auto-Connect**: Automatically connects to nearby machines, streamlining your power distribution.
- **Electric Pole**: 2-block tall structures for efficient long-distance transmission.
- **Advanced Rendering**: Realistic and smooth visual experience for electrical components.
- **Power Logic**: Integrated management for complex and scalable electrical networks.
- **KubeJS Support**: Create custom electric poles with configurable transfer rates, connection ranges, and wire colors.

## KubeJS Integration

NeoGrid registers a `neogrid:electric_pole` block builder type for KubeJS. Place scripts in `kubejs/startup_scripts/`.

```js
StartupEvents.registry('block', event => {
    event.create('kubejs:my_pole', 'neogrid:electric_pole')
        .transferRate(5000)       // FE/tick, default 1000
        .connectionRange(20)      // blocks, default 10
        .wireColorHex(0xFF3333)   // hex color
        .displayName('My Pole')
})
```

### Available Methods

| Method | Params | Description |
|--------|--------|-------------|
| `transferRate(rate)` | `int` | Max energy transfer rate (FE/tick) |
| `connectionRange(range)` | `int` | Auto-connection scan radius (blocks) |
| `wireColor(r, g, b)` | `float, float, float` (0.0~1.0) | Wire color as RGB floats |
| `wireColorHex(hex)` | `int` (e.g. `0xFF0000`) | Wire color as hex |

All standard KubeJS `BlockBuilder` methods (`displayName`, `hardness`, `resistance`, `mapColor`, `tag`, etc.) are also available.

See [docs/kubejs-integration.md](docs/kubejs-integration.md) for full examples.

---

# NeoGrid
NeoForge 平台的现代化自动化电网模组。

## 功能特点
- **自动连接**：自动识别并连接周边机器，极大简化电力分配流程。
- **电线杆**：2 格高的多方块结构，专为长距离电力传输设计。
- **高级渲染**：自定义渲染效果，提供逼真流畅的视觉体验。
- **电力逻辑**：集成电网管理逻辑，轻松应对复杂多变的电力需求。
- **KubeJS 支持**：通过 KubeJS 脚本自定义电线杆的传输速率、连接范围和电线颜色。

## KubeJS 集成

NeoGrid 注册了 `neogrid:electric_pole` 方块构建器类型。将脚本放在 `kubejs/startup_scripts/` 下。

```js
StartupEvents.registry('block', event => {
    event.create('kubejs:my_pole', 'neogrid:electric_pole')
        .transferRate(5000)       // FE/tick，默认 1000
        .connectionRange(20)      // 方块数，默认 10
        .wireColorHex(0xFF3333)   // 十六进制颜色
        .displayName('My Pole')
})
```

### 可用方法

| 方法 | 参数 | 说明 |
|------|------|------|
| `transferRate(rate)` | `int` | 最大能量传输速率（FE/tick） |
| `connectionRange(range)` | `int` | 自动连接扫描半径（方块数） |
| `wireColor(r, g, b)` | `float, float, float`（0.0~1.0） | RGB 浮点值设置电线颜色 |
| `wireColorHex(hex)` | `int`（如 `0xFF0000`） | 十六进制设置电线颜色 |

所有 KubeJS 标准 `BlockBuilder` 方法（`displayName`、`hardness`、`resistance`、`mapColor`、`tag` 等）均可使用。

详见 [docs/kubejs-integration.md](docs/kubejs-integration.md)。
