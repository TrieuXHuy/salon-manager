# 🎨 Design System - Salon Booking Manager

## Giới Thiệu

Tài liệu này định nghĩa hệ thống thiết kế toàn bộ ứng dụng Salon Booking Manager, bao gồm bảng màu, typography, components, spacing, và các quy tắc thiết kế.

---

## 🎭 Theme & Look & Feel

### FlatLaf Configuration

```java
// Light Theme (Mặc định)
UIManager.setLookAndFeel("com.formdev.flatlaf.FlatIntelliJLaf");

// Dark Theme
UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarculaLaf");

// Darkest Theme
UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarkLaf");
```

### Đặc Điểm Thiết Kế
- ✅ Flat Design (Không gradient, không viền dày)
- ✅ Borderless Components (Bo góc 8-12px)
- ✅ Modern Typography (Segoe UI, Inter, Roboto)
- ✅ Subtle Shadows (Bóng đổ nhẹ khi hover)
- ✅ Smooth Transitions (Hiệu ứng hover, animation)
- ✅ Accessibility Ready (High contrast, readable text)

---

## 🎨 Bảng Màu Chính

### Primary Colors

```java
// Navy - Màu chủ đạo
NAVY_COLOR = new Color(13, 27, 42)          // #0D1B2A
RGB: (13, 27, 42)
HSL: 210°, 53%, 11%
Sử dụng: Headers, titles, main text

// Emerald - Màu thành công
EMERALD_COLOR = new Color(16, 185, 129)     // #10B981
RGB: (16, 185, 129)
HSL: 160°, 84%, 39%
Sử dụng: Active state, success, primary buttons, badges (COMPLETED, ACTIVE)
```

### Secondary Colors

```java
// Amber - Màu cảnh báo
AMBER_COLOR = new Color(245, 158, 11)       // #F59E0B
RGB: (245, 158, 11)
HSL: 38°, 92%, 50%
Sử dụng: Warning, pending status, secondary elements

// Crimson - Màu lỗi/hủy
CRIMSON_COLOR = new Color(220, 38, 38)      // #DC2626
RGB: (220, 38, 38)
HSL: 0°, 84%, 50%
Sử dụng: Error, danger, cancelled status, delete actions

// Slate - Màu xám (không hoạt động)
SLATE_COLOR = new Color(100, 116, 139)      // #64748B
RGB: (100, 116, 139)
HSL: 217°, 16%, 47%
Sử dụng: Disabled state, inactive items
```

### Neutral Colors

```java
// Background
AMBIENT_LIGHT = new Color(242, 244, 246)    // #F2F4F6
RGB: (242, 244, 246)
HSL: 200°, 14%, 96%
Sử dụng: Main background, card backgrounds

// Surfaces
Color.WHITE = new Color(255, 255, 255)      // #FFFFFF
Sử dụng: Card, dialog, panel backgrounds

// Text Colors
TEXT_PRIMARY = new Color(17, 24, 39)        // #111827
RGB: (17, 24, 39)
HSL: 217°, 39%, 11%
Sử dụng: Main text, headings

TEXT_MUTED = new Color(107, 114, 128)       // #6B7280
RGB: (107, 114, 128)
HSL: 217°, 11%, 43%
Sử dụng: Secondary text, captions, disabled text

// Borders
new Color(229, 231, 235)                    // #E5E7EB
Sử dụng: Dividers, borders, subtle outlines
```

### Additional Colors

```java
// Blue (Info/Confirmed)
new Color(59, 130, 246)                     // #3B82F6
Sử dụng: CONFIRMED status, info messages

// Green (Success)
new Color(34, 197, 94)                      // #22C55E
Sử dụng: Success messages, earnings

// Purple (Accent)
new Color(168, 85, 247)                     // #A855F7
Sử dụng: Special features, premium elements
```

### Color Usage by Status

| Status | Color | RGB | Hex | Sử Dụng |
|--------|-------|-----|-----|---------|
| COMPLETED | Emerald | (16, 185, 129) | #10B981 | ✅ Hoàn thành |
| CONFIRMED | Blue | (59, 130, 246) | #3B82F6 | ✔️ Đã xác nhận |
| PENDING | Amber | (245, 158, 11) | #F59E0B | ⏳ Chờ xử lý |
| CANCELLED | Crimson | (220, 38, 38) | #DC2626 | ❌ Đã hủy |
| ACTIVE | Emerald | (16, 185, 129) | #10B981 | 🟢 Hoạt động |
| INACTIVE | Slate | (100, 116, 139) | #64748B | ⚪ Không hoạt động |

---

## 🔤 Typography System

### Font Families

```java
// Main Font (hiện đại, rõ ràng)
"Segoe UI"      // Windows, recommended
"Inter"         // Cross-platform
"Roboto"        // Android
"Arial"         // Fallback
"SansSerif"     // Java default fallback
```

### Font Sizes & Weights

```java
// Display / Hero (24pt, Bold)
titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));

// Heading Large (18pt, Bold)
heading1.setFont(new Font("Segoe UI", Font.BOLD, 18));

// Heading Medium (16pt, Medium/Semibold)
heading2.setFont(new Font("Segoe UI", Font.BOLD, 16));

// Heading Small (14pt, Bold)
heading3.setFont(new Font("Segoe UI", Font.BOLD, 14));

// Body Large (13pt, Plain)
bodyLarge.setFont(new Font("Segoe UI", Font.PLAIN, 13));

// Body Regular (12pt, Plain) - DEFAULT
body.setFont(new Font("Segoe UI", Font.PLAIN, 12));

// Body Small (11pt, Plain)
bodySmall.setFont(new Font("Segoe UI", Font.PLAIN, 11));

// Monospace (12pt, Plain) - For code
mono.setFont(new Font("Consolas", Font.PLAIN, 12));
```

### Typography Usage

```
Page Title:     28pt Bold Navy      (e.g., "Dashboard", "Quản Lý Lịch Hẹn")
Section Title:  16pt Bold Navy      (e.g., "📋 Thông Tin Chung")
Card Title:     14pt Bold Navy      (e.g., "Doanh Thu Hôm Nay")
Primary Text:   12pt Plain Navy     (e.g., Table content)
Secondary Text: 12pt Plain Muted    (e.g., Captions, hints)
Muted Text:     11pt Plain Muted    (e.g., Timestamps, labels)
```

---

## 🔲 Components Style Guide

### Buttons

#### Primary Button
```java
// Style
Background: Emerald (#10B981)
Text: White
Border Radius: 8px
Padding: 10px 20px
Font: Segoe UI, 12pt Bold
Height: 38px

// States
Normal:   Background Emerald
Hover:    Background Darker Emerald (#13a873)
Pressed:  Background Darker Emerald (#0d8a60)
Disabled: Background Slate, Text Muted
```

#### Secondary Button
```java
// Style
Background: Light Gray (#F3F4F6)
Text: Muted Gray
Border Radius: 8px
Padding: 10px 20px
Font: Segoe UI, 12pt Plain
Height: 38px

// States
Normal:   Background Light Gray
Hover:    Background Lighter (#E5E7EB)
Pressed:  Background Gray (#E5E7EB)
```

#### Small Button (Action)
```java
// Style
Background: Emerald
Text: White
Border Radius: 6px
Padding: 6px 12px
Font: Segoe UI, 11pt Plain
Height: 28px
```

### Cards

```java
// Style
Background: White
Border: 1px #E5E7EB
Border Radius: 12px
Padding: 20px
Shadow: Subtle (only on hover)
Box Shadow: 0 0 0 1px rgba(0, 0, 0, 0.05)

// Hover Effect
Border Color: Emerald (for some cards)
Shadow: 0 10px 15px rgba(0, 0, 0, 0.1)
Cursor: Hand Pointer
```

### Status Badges

```java
// Style
Border Radius: 6px
Padding: 4px 12px
Font: Segoe UI, 11pt Bold
Width: Auto (min 70px)
Height: 28px

// Variants
COMPLETED:  Background Emerald,   Text White
CONFIRMED:  Background Blue,      Text White
PENDING:    Background Amber,     Text White
CANCELLED:  Background Crimson,   Text White
INACTIVE:   Background Slate,     Text White
```

### Input Fields

```java
// Style
Background: Ambient Light (#F2F4F6)
Border: 1px #E5E7EB
Border Radius: 6px
Padding: 8px 12px
Font: Segoe UI, 12pt Plain
Height: 36px
Text Color: Navy

// States
Focus:      Border Emerald, Shadow 0 0 0 3px rgba(16, 185, 129, 0.1)
Disabled:   Background Slate, Text Muted
Error:      Border Crimson
```

### Tables

```java
// Header Row
Background: White
Text: 12pt Bold Muted
Border Bottom: 1px #E5E7EB
Height: 40px
Padding: 0 12px

// Data Row
Background: Ambient Light
Height: 45px (or 40px)
Padding: 0 12px
Border Bottom: 1px #F2F4F6
Text: 12pt Plain Navy

// Alternating Rows (Optional)
Row 1: Ambient Light
Row 2: White

// Selected Row
Background: Ambient Light + Emerald tint
Color: Light blue (#DBF2F1)

// Hover Row (Optional)
Background: Slightly darker
Cursor: Default (or Hand for clickable)
```

### Dialog Boxes

```java
// Style
Background: White
Border Radius: 12px
Shadow: 0 20px 25px rgba(0, 0, 0, 0.15)
Padding: 20px
Title Font: 16pt Bold Navy
Content Font: 12pt Plain Navy

// Buttons
Confirm: Primary Button (Emerald)
Cancel: Secondary Button (Gray)
```

---

## 📏 Spacing System

### Standard Spacing Values

```java
// 8px grid system
4px   - Extra small gaps
8px   - Small gaps, internal padding
12px  - Medium gaps, table cells
16px  - Large gaps, section gaps
20px  - Extra large gaps, panel padding
24px  - Heading spacing
30px  - Page padding, major sections
40px  - Layout sections

// Usage
BorderFactory.createEmptyBorder(20, 30, 20, 30)  // Top, Left, Bottom, Right
Box.createVerticalStrut(15)                       // Vertical spacing
Box.createHorizontalStrut(20)                     // Horizontal spacing
```

### Padding Guidelines

```java
// Small Components (Buttons, Badges)
Vertical: 6-10px
Horizontal: 12-16px

// Cards & Panels
Vertical: 20-25px
Horizontal: 20-25px

// Page/Container
Vertical: 30px
Horizontal: 30px

// Section Headers
Bottom: 20px
```

### Margin Guidelines

```java
// Between Components
Horizontal: 15-20px
Vertical: 15-20px

// Between Sections
Vertical: 30-40px
```

---

## 🎯 Layout Guidelines

### Desktop Layout (1400x900 - Default)

```
┌─────────────────────────────────────┐
│     Header (70px height)            │
├────────────┬──────────────────────────┤
│ Sidebar    │   Main Content Area      │
│ (280px)    │   (CardLayout)           │
│            │                          │
│  - Menu    │   ┌──────────────────┐  │
│  - Items   │   │ Dashboard        │  │
│  - Icons   │   │ Appointments     │  │
│            │   │ Customers        │  │
│            │   │ Services         │  │
│            │   │ Staff            │  │
│            │   │ Settings         │  │
│            │   └──────────────────┘  │
└────────────┴──────────────────────────┘
```

### Responsive Breakpoints

```
Desktop:  >= 1200px width
Tablet:   768px - 1199px
Mobile:   < 768px
(Note: Current version designed for Desktop)
```

### Grid System

```
Main Grid:      GridLayout(2, 2, 20, 20)  // 2x2 metric cards
Service Cards:  GridLayout(2, 3, 20, 20)  // 2x3 service cards
Table:          Column-based layout
```

---

## ✨ Visual Effects & Animations

### Hover Effects

```java
// Subtle Elevation
Card hover:     Add box shadow
Button hover:   Change color slightly
Row hover:      Light background change

// Cursor Changes
Card items:     Cursor.HAND_CURSOR
Buttons:        Cursor.HAND_CURSOR
Tables:         Cursor.DEFAULT_CURSOR
```

### Shadows

```java
// Subtle Shadow (cards on normal state)
No shadow (only border)

// Medium Shadow (cards on hover)
g2d.setColor(new Color(0, 0, 0, 15));
for (int i = 0; i < 10; i++) {
    g2d.setComposite(AlphaComposite.getInstance(
        AlphaComposite.SRC_OVER, opacity * 0.05f
    ));
    g2d.fillRoundRect(i, i, width - 2*i, height - 2*i, 12, 12);
}

// Dialog Shadow (larger)
0 20px 25px rgba(0, 0, 0, 0.15)
```

### Border Radius

```java
// Small Components (buttons, badges)
6-8px

// Cards & Dialogs
12px

// Panel Corners
10px
```

---

## 🎭 Dark Mode (Future Implementation)

```java
// Dark Theme Colors
NAVY_DARK = new Color(30, 41, 59)           // Darker background
TEXT_PRIMARY_DARK = new Color(248, 250, 252) // Light text
TEXT_MUTED_DARK = new Color(148, 163, 184)   // Muted light text
BG_CARD_DARK = new Color(51, 65, 85)        // Card background
```

---

## ♿ Accessibility Features

### Color Contrast

```
Text on Background:
- Navy (13, 27, 42) on White:     Ratio 7.5:1 ✅ AAA
- Navy on Light Gray:              Ratio 6.2:1 ✅ AA
- Emerald on White:                Ratio 4.1:1 ✅ AA
- Muted (107, 114, 128) on White:  Ratio 4.8:1 ✅ AA
```

### Focus Indicators

```java
// When component has focus, show clear outline
Border-Color: Emerald
Outline: 2px Emerald outline
```

### Font Sizes

```
Minimum: 11pt (only for very secondary text)
Default: 12pt (body text)
Headings: 16pt+ (clearly different)
```

---

## 🔄 Component States Cheat Sheet

```
BUTTON STATES:
├─ Normal:    [Background Color] + Navy Text
├─ Hover:     [Darker Background] + Navy Text + Cursor Hand
├─ Pressed:   [Even Darker] + Navy Text
├─ Disabled:  Slate Background + Muted Text
└─ Focus:     [Color] + 2px Emerald Outline

CARD STATES:
├─ Normal:    White + Gray Border
├─ Hover:     White + Emerald Border + Shadow
└─ Selected:  Slightly tinted background

TABLE ROW STATES:
├─ Normal:    Ambient Light background
├─ Hover:     Light tint
└─ Selected:  Emerald tint (#DBF2F1)

STATUS BADGE STATES:
├─ Completed:  Emerald background
├─ Confirmed:  Blue background
├─ Pending:    Amber background
├─ Cancelled:  Crimson background
└─ Inactive:   Slate background
```

---

## 📐 Design Tokens Summary

```json
{
  "colors": {
    "primary": "#10B981",
    "secondary": "#F59E0B",
    "danger": "#DC2626",
    "info": "#3B82F6",
    "success": "#22C55E",
    "warning": "#F59E0B",
    "background": "#F2F4F6",
    "surface": "#FFFFFF",
    "text": "#111827",
    "text-muted": "#6B7280",
    "border": "#E5E7EB"
  },
  "typography": {
    "font-family": "Segoe UI, Inter, Roboto, Arial",
    "font-size": {
      "xs": "11px",
      "sm": "12px",
      "base": "13px",
      "lg": "14px",
      "xl": "16px",
      "2xl": "18px",
      "3xl": "24px",
      "4xl": "28px"
    },
    "font-weight": {
      "normal": 400,
      "medium": 500,
      "semibold": 600,
      "bold": 700
    }
  },
  "spacing": {
    "xs": "4px",
    "sm": "8px",
    "md": "12px",
    "lg": "16px",
    "xl": "20px",
    "2xl": "24px",
    "3xl": "30px",
    "4xl": "40px"
  },
  "border-radius": {
    "sm": "6px",
    "md": "8px",
    "lg": "10px",
    "xl": "12px"
  }
}
```

---

## 📖 Design Principles

1. **Clarity**: Rõ ràng, dễ hiểu, không nhập nhằng
2. **Consistency**: Thống nhất trên toàn ứng dụng
3. **Feedback**: Mỗi hành động có phản hồi rõ ràng
4. **Efficiency**: Giảm thiểu số bước, tối ưu tác vụ
5. **Aesthetics**: Đẹp mắt, thẩm mỹ cao cấp
6. **Accessibility**: Dễ sử dụng cho mọi người

---

## 🎓 Design Reference

- **Inspiration**: Material Design, Apple HIG, Fluent Design
- **Library**: FlatLaf
- **Tools Used**: Graphics2D, Java Swing

---

**Design System Version**: 1.0.0
**Last Updated**: May 2026
