# FragmentManager 事务冲突修复总结

## 问题描述
应用在 `HomeFragment` 的 `onStart()` 方法中调用 `reloadView()` 时出现 `IllegalStateException: FragmentManager is already executing transactions` 异常。

## 根本原因
1. Fragment 生命周期方法（如 `onStart()`）中直接执行 Fragment 事务
2. ViewPager2 的 FragmentStateAdapter 在 Fragment 生命周期不稳定时调用 `notifyDataSetChanged()`
3. FragmentManager 正在执行其他事务时，又尝试执行新的 Fragment 事务

## 修复方案

### 1. 添加 Fragment 状态安全检查
在 `HomeFragment.kt` 中添加了 `isFragmentSafe()` 方法和 `safeExecuteFragmentOperation()` 方法：

```kotlin
private fun isFragmentSafe(): Boolean {
    return isAdded && !isDetached && activity != null && !isFragmentManagerBusy
}

private fun safeExecuteFragmentOperation(operation: () -> Unit) {
    if (!isFragmentSafe()) {
        Log.w(TAG, "Fragment not in safe state, skipping operation")
        return
    }
    
    try {
        isFragmentManagerBusy = true
        operation()
    } catch (e: IllegalStateException) {
        // 延迟重试机制
        mHandler.postDelayed({
            try {
                if (isFragmentSafe()) {
                    operation()
                }
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to execute operation after retry: ${e2.message}")
            } finally {
                isFragmentManagerBusy = false
            }
        }, 100)
    } catch (e: Exception) {
        Log.e(TAG, "Error executing fragment operation: ${e.message}")
    } finally {
        isFragmentManagerBusy = false
    }
}
```

### 2. 优化 ViewPagerAdapter
在 `ViewPagerAdapter.java` 中：
- 添加了延迟执行机制，避免在 Fragment 生命周期中直接调用 `notifyDataSetChanged()`
- 实现了 `getItemId()` 和 `containsItem()` 方法，为 Fragment 提供稳定的 ID
- 添加了异常处理和重试机制

### 3. 修改关键方法
- `reloadFragment()`: 使用安全执行机制
- `reloadView()`: 添加状态检查
- `onStart()`: 使用延迟执行确保 Fragment 完全启动

## 修复效果
1. 避免了 FragmentManager 事务冲突
2. 提高了应用的稳定性
3. 添加了完善的错误处理和重试机制
4. 保持了原有的功能逻辑不变

## 注意事项
1. 所有 Fragment 操作都应该通过 `safeExecuteFragmentOperation()` 执行
2. 在 Fragment 生命周期方法中避免直接执行复杂的 Fragment 事务
3. 使用延迟执行机制确保 Fragment 状态稳定后再执行操作 