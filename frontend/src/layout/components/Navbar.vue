<template>
  <div class="navbar">
    <hamburger :is-active="sidebar.opened" class="hamburger-container" @toggleClick="toggleSideBar" />

    <breadcrumb class="breadcrumb-container" />

    <div class="right-menu">
      <el-dropdown class="avatar-container" trigger="click">
        <div class="avatar-wrapper">
          <img :src="avatar" class="user-avatar">
          <el-icon class="el-icon-caret-bottom"><CaretBottom /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu class="user-dropdown">
            <router-link to="/myself">
              <el-dropdown-item> 个人中心 </el-dropdown-item>
            </router-link>
            <router-link to="/change-password">
              <el-dropdown-item> 修改密码</el-dropdown-item>
            </router-link>

            <el-dropdown-item divided @click="logout">
              <span style="display: block">退出登录</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script>
import { mapState, mapActions } from 'pinia'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import { useTagsViewStore } from '@/stores/tagsView'
import Breadcrumb from '@/components/Breadcrumb'
import Hamburger from '@/components/Hamburger'

export default {
  components: {
    Breadcrumb,
    Hamburger
  },
  computed: {
    ...mapState(useAppStore, ['sidebar']),
    ...mapState(useUserStore, ['avatar']),
    ...mapState(useTagsViewStore, ['tags'])
  },
  methods: {
    ...mapActions(useAppStore, ['toggleSideBar']),
    ...mapActions(useUserStore, { logoutUser: 'logout' }),
    ...mapActions(useTagsViewStore, { removeTag: 'removeTag' }),
    handleTagClose(item) {
      this.removeTag(item)
    },
    async logout() {
      try {
        await this.$confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await this.logoutUser()
        this.$router.push(`/login`)
        this.$message.success('成功退出')
      } catch (error) {
        if (error !== 'cancel') {
          console.error('Logout failed:', error)
          this.$message.error('退出失败，请重试')
        }
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.el-tag {
  background-color: #ffffff;
  border-color: #cacaca;
  display: inline-block;
  height: 32px;
  padding: 0 10px;
  line-height: 30px;
  margin-left: 5px;
  font-size: 12px;
  color: #000000;
  border-width: 1.5px;
  border-style: solid;
  border-radius: 3px;
  -webkit-box-sizing: border-box;
  box-sizing: border-box;
  white-space: nowrap;
}

.active {
  background-color: #58b289;
  color: rgb(255, 255, 255);
}

.navbar {
  height: 110px;
  overflow: hidden;
  position: relative;
  background: #fff;
  box-shadow: 0 1px 4px rgb(0 21 41 / 16%);

  .hamburger-container {
    line-height: 46px;
    height: 100%;
    float: left;
    cursor: pointer;
    transition: background 0.3s;
    -webkit-tap-highlight-color: transparent;

    &:hover,
    &:active {
      background: rgba(0, 0, 0, 0.025);
    }
  }

  .breadcrumb-container {
    float: left;
  }

  .right-menu {
    float: right;
    height: 100%;
    line-height: 50px;

    &:focus {
      outline: none;
    }

    .right-menu-item {
      display: inline-block;
      padding: 0 8px;
      height: 100%;
      font-size: 18px;
      color: #5a5e66;
      vertical-align: text-bottom;

      &.hover-effect {
        cursor: pointer;
        transition: background 0.3s;

        &:hover,
        &:active {
          background: rgba(0, 0, 0, 0.025);
        }
      }
    }

    .avatar-container {
      margin-right: 30px;

      .avatar-wrapper {
        margin-top: 5px;
        position: relative;

        .user-avatar {
          cursor: pointer;
          width: 40px;
          height: 40px;
          border-radius: 10px;
        }

        .el-icon-caret-bottom {
          cursor: pointer;
          position: absolute;
          right: 15px;
          top: 95%;
          transform: translateY(-50%);
          font-size: 12px;
        }
      }
    }
  }
}

@media (max-width: 991px) {
  .navbar {
    height: 60px;

    .hamburger-container {
      line-height: 60px;
    }

    .right-menu {
      line-height: 60px;

      .avatar-container {
        margin-right: 15px;

        .avatar-wrapper {
          margin-top: 10px;
        }
      }
    }
  }
}
</style>
