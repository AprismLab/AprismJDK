# AprismJDK Maven Central Publishing Guide

## 命名空间验证

你已注册的Maven Central命名空间：
- **Namespace**: `com.aprismlab`
- **Verification Key**: `p48mc1cw49`
- **Status**: Verification Pending

### 验证步骤

#### 方法1：GitHub验证文件（已完成）✅
验证文件已创建并可访问：
- 文件位置：`.well-known/central-publisher-verification.txt`
- 访问URL：https://raw.githubusercontent.com/AprismLab/AprismJDK/main/.well-known/central-publisher-verification.txt
- 内容：`p48mc1cw49`

#### 方法2：GitHub Releases验证
在GitHub仓库创建一个release，标题包含验证密钥：
```
Release标题：Maven Central Verification - p48mc1cw49
或者在release描述中包含：p48mc1cw49
```

#### 方法3：DNS TXT记录验证
在域名 `aprismlab.com` 添加TXT记录：
```
记录名：central-publisher-verification
记录值：p48mc1cw49
或者：
记录名：_central-publisher
记录值：p48mc1cw49
```

#### 方法4：手动触发验证
1. 登录 https://central.sonatype.com/
2. 进入 "Namespaces" 页面
3. 找到 `com.aprismlab` 命名空间
4. 点击 "Verify" 或 "Check Verification" 按钮
5. 系统会检查GitHub仓库中的验证文件

### 常见问题

**Q: 验证文件已存在但状态仍是 Pending？**
- 等待5-10分钟让Central Portal的爬虫检测
- 在Portal中手动点击"Verify"按钮
- 确认GitHub仓库是公开的
- 检查仓库所有者与注册的组织名称匹配

**Q: GitHub组织名称与包名不完全一致？**
- GitHub: `AprismLab`
- Package: `com.aprismlab`
- 这是允许的，但需要在Portal中说明仓库URL

## 凭据配置

凭据已保存在 `local.properties` (不会提交到git)：
```properties
mavenCentralUsername=o59htr
mavenCentralPassword=V8v3TkpiZR2BVqSANb4lEJDYM3aOzXa58
```

## 发布流程

### 1. 准备发布

```bash
# 确保所有测试通过
./gradlew test

# 生成POM文件验证
./gradlew :aprismate-api:generatePomFileForMavenJavaPublication

# 构建所有模块
./gradlew build
```

### 2. 发布到Maven Central

```bash
# 发布所有模块（跳过签名）
./gradlew publish -x signMavenJavaPublication

# 或者发布单个模块
./gradlew :aprismate-api:publish -x signMavenJavaPublication
./gradlew :aprismate-agent:publish -x signMavenJavaPublication
```

### 3. Maven Central新流程

Maven Central现在使用 **Central Portal** 发布流程：

1. **上传**: 使用 `publish` 任务上传构件
2. **验证**: Central Portal会自动验证构件
3. **发布**: 在Portal中点击 "Publish" 按钮

或者使用自动发布模式（需要在Portal中配置）。

## 发布的模块

- `com.aprismlab:aprismate-api:v26.1-Alpha.5`
- `com.aprismlab:aprismate-agent:v26.1-Alpha.5`

## 构件要求

Maven Central要求：
- [x] POM文件
- [x] Sources JAR
- [x] Javadoc JAR
- [ ] GPG签名 (可选，Alpha版本可暂不签名)

## 签名配置（可选）

如果需要GPG签名，在 `local.properties` 添加：
```properties
signing.keyId=你的GPG密钥ID后8位
signing.password=GPG密钥密码
signing.secretKeyRingFile=~/.gnupg/secring.gpg路径
```

## 验证发布

发布后可以在以下位置查看：
- Maven Central Portal: https://central.sonatype.com/
- Maven Central Search: https://search.maven.org/search?q=g:com.aprismlab

## 在项目中使用

```gradle
dependencies {
    implementation 'com.aprismlab:aprismate-api:v26.1-Alpha.5'
}
```

```xml
<dependency>
    <groupId>com.aprismlab</groupId>
    <artifactId>aprismate-api</artifactId>
    <version>v26.1-Alpha.5</version>
</dependency>
```

## 注意事项

1. **命名空间验证**: 必须先完成验证才能发布
2. **版本规范**: Alpha版本使用 `v26.1-Alpha.5` 格式
3. **发布不可撤回**: 一旦发布到Maven Central无法删除
4. **审核时间**: 首次发布可能需要几小时审核

## 故障排查

### 401 Unauthorized
检查 `local.properties` 中的凭据是否正确

### 403 Forbidden
命名空间可能未验证，检查验证状态

### 签名失败
如果不需要签名，可以在发布命令中添加：
```bash
./gradlew publish -x signMavenJavaPublication
```

### 验证一直Pending
1. 等待5-10分钟
2. 在Portal手动点击"Verify"
3. 尝试DNS验证方式
4. 检查GitHub仓库是公开的
