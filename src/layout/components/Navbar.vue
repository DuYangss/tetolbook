        <el-dropdown-menu>
          <router-link to="/profile">
            <el-dropdown-item>
              <el-icon><User /></el-icon>
              个人中心
            </el-dropdown-item>
          </router-link>
          <el-dropdown-item divided @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            退出登录
          </el-dropdown-item>
        </el-dropdown-menu> 