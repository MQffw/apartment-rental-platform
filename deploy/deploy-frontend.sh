#!/usr/bin/env bash
# ============================================================
# lease 前端一键部署到 102（server02）的 nginx
#
# 目标路径已按线上实际配置写死：
#   admin  PC 管理端  /usr/share/nginx/html/admin  反代 /admin -> 101:8080
#   app    H5 用户端  /usr/share/nginx/html/app    反代 /app   -> 101:8081
#   备份目录          /opt/deploy-backup
#
# 用法：
#   bash deploy/deploy-frontend.sh                  # 默认部署 admin：构建+上传+备份+替换+验证
#   bash deploy/deploy-frontend.sh --target app     # 只部署 H5 用户端
#   bash deploy/deploy-frontend.sh --target all     # 两端都部署
#   bash deploy/deploy-frontend.sh --skip-build     # 跳过构建，直接上传现有 dist
#   bash deploy/deploy-frontend.sh --probe          # 只探测远端现状，不改任何东西
#   bash deploy/deploy-frontend.sh --rollback       # 回滚到最近一次备份
#
# 可覆盖环境变量：
#   DEPLOY_HOST=192.168.10.102  DEPLOY_USER=root  DEPLOY_ROOT=/usr/share/nginx/html
# ============================================================
set -euo pipefail

HOST="${DEPLOY_HOST:-192.168.10.102}"
REMOTE_USER="${DEPLOY_USER:-root}"
REMOTE_ROOT="${DEPLOY_ROOT:-/usr/share/nginx/html}"
BACKUP_DIR="/opt/deploy-backup"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
SSH_OPTS=(-o BatchMode=yes -o ConnectTimeout=8 -o LogLevel=ERROR)

TARGETS="admin"
SKIP_BUILD=0
PROBE=0
ROLLBACK=0
while [ "$#" -gt 0 ]; do
  case "$1" in
    --probe) PROBE=1 ;;
    --skip-build) SKIP_BUILD=1 ;;
    --rollback) ROLLBACK=1 ;;
    --target=*) TARGETS="${1#*=}" ;;
    --target) shift; TARGETS="${1:-}" ;;
    --host=*) HOST="${1#*=}" ;;
    -h|--help) sed -n '2,20p' "$0"; exit 0 ;;
    *) echo "未知参数：$1"; exit 1 ;;
  esac
  shift
done
[ "$TARGETS" = "all" ] && TARGETS="admin app"
SSH_TARGET="$REMOTE_USER@$HOST"

# 每个目标的差异项
cfg() {
  case "$1" in
    admin)
      SITE=admin
      PROJECT_DIR="$REPO_DIR/frontend/rentHouseAdmin"
      HASH_PAT='assets/index-[a-z0-9]+\.js'
      VERIFY_HOST=""
      API_PATH="/admin/login/captcha"
      SITE_HINT="http://admin.lease.atguigu.com/"
      ;;
    app)
      SITE=app
      PROJECT_DIR="$REPO_DIR/frontend/rentHouseH5"
      HASH_PAT='static/js/index-[a-z0-9]+\.js'
      VERIFY_HOST="lease.atguigu.com"
      API_PATH="/app/apartment/pageItem?current=1&size=2"
      SITE_HINT="http://lease.atguigu.com/"
      ;;
    *) echo "未知 target：$1（可选 admin / app / all）"; exit 1 ;;
  esac
  SLOT="$REMOTE_ROOT/$SITE"
}

# 带上 Host 头访问本机 nginx（app 站靠 server_name 区分）
curl_local() {
  local path="$1" fmt="$2"
  if [ -n "$VERIFY_HOST" ]; then
    ssh "${SSH_OPTS[@]}" "$SSH_TARGET" "curl -s -H 'Host: $VERIFY_HOST' -o /dev/null -w '$fmt' --max-time 5 'http://127.0.0.1$path'" 2>/dev/null || echo "000"
  else
    ssh "${SSH_OPTS[@]}" "$SSH_TARGET" "curl -s -o /dev/null -w '$fmt' --max-time 5 'http://127.0.0.1$path'" 2>/dev/null || echo "000"
  fi
}

# 连通性前置检查
if ! ssh "${SSH_OPTS[@]}" "$SSH_TARGET" true 2>/dev/null; then
  echo "!! 无法免密登录 $SSH_TARGET"
  echo "   在目标机 root 登录状态下执行："
  echo "   mkdir -p ~/.ssh && chmod 700 ~/.ssh"
  echo "   echo '<你的公钥>' >> ~/.ssh/authorized_keys && chmod 600 ~/.ssh/authorized_keys"
  exit 1
fi

# ---------- 探测 ----------
if [ "$PROBE" = "1" ]; then
  echo "==> nginx 站点配置："
  ssh "${SSH_OPTS[@]}" "$SSH_TARGET" \
    "nginx -T 2>/dev/null | grep -vE '^\s*#' | grep -nE 'server_name|^\s*listen|^\s*root |proxy_pass|^\s*location|client_max_body_size'"
  for t in $TARGETS; do
    cfg "$t"
    echo
    echo "==> [$SITE] 站点目录：$SLOT"
    ssh "${SSH_OPTS[@]}" "$SSH_TARGET" "ls -l $SLOT 2>/dev/null; echo; stat -c 'index.html 时间 %y' $SLOT/index.html 2>/dev/null; du -sh $SLOT 2>/dev/null"
  done
  echo
  echo "==> 已有备份："
  ssh "${SSH_OPTS[@]}" "$SSH_TARGET" "ls -1t $BACKUP_DIR/ 2>/dev/null | head -8 || echo '(无)'"
  exit 0
fi

# ---------- 回滚 ----------
if [ "$ROLLBACK" = "1" ]; then
  for t in $TARGETS; do
    cfg "$t"
    echo "==> [$SITE] 回滚：使用最近一次备份"
    ssh "${SSH_OPTS[@]}" "$SSH_TARGET" \
      ROOT="$REMOTE_ROOT" SITE="$SITE" BAK="$BACKUP_DIR" 'bash -s' <<'REMOTE'
set -e
LAST=$(ls -1t "$BAK/$SITE"-*.tar.gz 2>/dev/null | head -1)
if [ -z "$LAST" ]; then echo "!! 没有可用备份"; exit 1; fi
echo "  使用：$LAST"
TS=$(date +%Y%m%d-%H%M%S)
mv "$ROOT/$SITE" "$BAK/$SITE-rollback-$TS"
mkdir -p "$ROOT/$SITE"
tar xzf "$LAST" -C "$ROOT"
echo "  已回滚，当前文件数：$(find "$ROOT/$SITE" -type f | wc -l)"
echo "  回滚前的版本留在：$BAK/$SITE-rollback-$TS"
REMOTE
  done
  exit 0
fi

# ---------- 逐个目标：构建 / 上传 / 替换 / 验证 ----------
for t in $TARGETS; do
  cfg "$t"
  echo
  echo "=============== [$SITE] $PROJECT_DIR ==============="

  if [ "$SKIP_BUILD" = "0" ]; then
    cd "$PROJECT_DIR"
    # 生产包接口地址必须同源，否则请求打到教程域名（app 站的原值 lease.atguigu.com 也建议改成空）
    BASE_URL=$(grep -E "^VITE_APP_BASE_URL=" .env.production 2>/dev/null | cut -d= -f2- || true)
    if [ -n "$BASE_URL" ] && [ "$BASE_URL" != "''" ] && [ "$BASE_URL" != '""' ]; then
      echo "!! .env.production 的 VITE_APP_BASE_URL=$BASE_URL 不是同源相对路径"
      echo "   改成：VITE_APP_BASE_URL=''   然后重跑（或加 --skip-build 用现有 dist）"
      exit 1
    fi
    echo "==> npm run build"
    npm run build
  fi

  cd "$PROJECT_DIR"
  [ -f dist/index.html ] || { echo "!! dist/index.html 不存在，构建失败"; exit 1; }
  LOCAL_HASH=$(grep -oE "$HASH_PAT" dist/index.html | head -1)
  echo "==> 产物：$(find dist -type f | wc -l) 个文件，入口 ${LOCAL_HASH:-未识别}"

  TMP_TGZ="$(mktemp -t lease-dist-XXXXXX).tgz"
  trap 'rm -f "$TMP_TGZ"' EXIT
  tar czf "$TMP_TGZ" -C dist .
  REMOTE_TGZ="/tmp/$SITE-dist.tgz"
  echo "==> 上传 $(du -h "$TMP_TGZ" | cut -f1)"
  scp -q "${SSH_OPTS[@]}" "$TMP_TGZ" "$SSH_TARGET:$REMOTE_TGZ"

  echo "==> 远端备份并替换"
  ssh "${SSH_OPTS[@]}" "$SSH_TARGET" \
    ROOT="$REMOTE_ROOT" SITE="$SITE" TGZ="$REMOTE_TGZ" BAK="$BACKUP_DIR" 'bash -s' <<'REMOTE'
set -e
TS=$(date +%Y%m%d-%H%M%S)
SLOT="$ROOT/$SITE"
mkdir -p "$BAK"
if [ -d "$SLOT" ] && [ -n "$(ls -A "$SLOT" 2>/dev/null)" ]; then
  tar czf "$BAK/$SITE-$TS.tar.gz" -C "$ROOT" "$SITE"
  echo "  备份：$BAK/$SITE-$TS.tar.gz ($(du -h "$BAK/$SITE-$TS.tar.gz" | cut -f1))"
fi
NEW="/tmp/$SITE-new-$TS"
mkdir -p "$NEW"
tar xzf "$TGZ" -C "$NEW"
find "$NEW" -type d -exec chmod 755 {} +
find "$NEW" -type f -exec chmod 644 {} +
chown -R root:root "$NEW"
[ -d "$SLOT" ] && mv "$SLOT" "$BAK/$SITE-old-$TS"
mv "$NEW" "$SLOT"
ls -1t "$BAK/$SITE"-*.tar.gz 2>/dev/null | tail -n +6 | xargs -r rm -f
ls -1dt "$BAK/$SITE-old-"* 2>/dev/null | tail -n +3 | xargs -r rm -rf
rm -f "$TGZ"
echo "  完成，文件数：$(find "$SLOT" -type f | wc -l)"
REMOTE

  echo "==> 验证"
  REMOTE_HASH=$(ssh "${SSH_OPTS[@]}" "$SSH_TARGET" \
    "curl -s -H 'Host: ${VERIFY_HOST:-127.0.0.1}' http://127.0.0.1/ | grep -oE '$HASH_PAT' | head -1" 2>/dev/null || true)
  CODE=$(curl_local "/" "%{http_code}")
  API=$(curl_local "$API_PATH" "%{http_code}")
  CACHE=$(ssh "${SSH_OPTS[@]}" "$SSH_TARGET" \
    "curl -s -I -H 'Host: ${VERIFY_HOST:-127.0.0.1}' http://127.0.0.1/index.html | grep -i '^cache-control' | tr -d '\r'" 2>/dev/null || true)

  echo "  首页        HTTP $CODE"
  echo "  入口脚本    ${REMOTE_HASH:-未取到}"
  echo "  接口反代    HTTP $API  ($API_PATH)"
  echo "  入口缓存    ${CACHE:-（无缓存头）}"

  # 入口 HTML 没禁用缓存时，发版后浏览器仍会加载旧的 hash 资源，表现为「界面没变」
  if ! echo "$CACHE" | grep -qi "no-store"; then
    echo
    echo "!! index.html 缺少 no-store 缓存头，发版后用户浏览器可能仍显示旧界面"
    echo "   在 $SITE 的 server 块加：location = /index.html { add_header Cache-Control \"no-store, no-cache, must-revalidate\" always; }"
  fi

  if [ "$REMOTE_HASH" != "$LOCAL_HASH" ]; then
    echo
    echo "!! 入口脚本与本地产物不一致（本地 $LOCAL_HASH），可能是缓存或替换未生效"
    echo "   排查：ssh $SSH_TARGET 'grep -oE \"$HASH_PAT\" $SLOT/index.html'"
    exit 1
  fi
  if [ "$API" = "502" ] || [ "$API" = "504" ]; then
    echo
    echo "!! 反代失败，101 上的后端可能没起"
    echo "   admin -> ssh root@192.168.10.101 'systemctl status lease-admin'"
    echo "   app   -> ssh root@192.168.10.101 'systemctl status lease-app'"
    exit 1
  fi

  echo "==> [$SITE] 部署成功，访问：$SITE_HINT"
done

echo
echo "回滚命令：bash deploy/deploy-frontend.sh --target <admin|app> --rollback"
