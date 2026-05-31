<template>
  <div class="topicDetail_replyItem">
    <div class="topicDetail_info">
      <p>
        <img class="topicDetail_avatar" :src="localDiscussionData.avatar" />
        <span class="author">{{ localDiscussionData.realName }}</span>
        <span class="department"></span>
      </p>
      <div class="topic_interactive">
        <!-- el-icon-thumb -->
        <!-- <div class="like" :style="isLike==0:background=">点赞<i class="el-icon-thumb"></i></i><span></span></div> -->
        <div class="like">
          <span class="text-white p-2">
            <el-icon
            v-if="currentRole === 'teacher' || currentRole === 'admin'"
            @click="showIsDel(localDiscussionData.id)"
            ><Delete /></el-icon>
            <span
              :class="{ 'bg-green-500': localDiscussionData.isLike === 1, 'bg-gray-300': localDiscussionData.isLike === 0 }"
              style="margin-left: 30px;"
              @click="toggleLike"
            ><el-icon><Star /></el-icon> 点赞 {{ localDiscussionData.count }}</span>
          </span>
        </div>


      </div>
    </div>
    <div class="topicReply_right">
      <div class="otherInfo">
        <span class="customedTime" bbsid="866df1b04e0e18910f00ab182cf82963">{{
          localDiscussionData.createTime
        }}</span>
      </div>
      <SafeHtml class="replyContent" :content="localDiscussionData.content" />
    </div>
  </div>
</template>
<script>
import {doLike} from "@/api/discussion"
import {getUserId} from '@/utils/auth'
import {getRole} from '@/utils/jwtUtils'
import SafeHtml from '@/components/SafeHtml'
export default {
  components: {
    SafeHtml
  },
  props: {
    discussionId: {
      type: Number
    },
    onConfirm: {
      type: Function,
      default: () => {},
    },
    delFun: {
      type: Function,
      default: () => {},
    },

    discussionData: {
      type: Object,
      default() {
        return {
          // id: 12,
          // userId: 164,
          // parentId: -1,
          // avatar: "https://online-exam-system-backend.oss-cn-beijing.aliyuncs.com/da93c2a6-6879-46c3-b38f-a99956f70d22.jpg",
          // content: "2223213",
          // realName: "学生测试账号",
          // count: 0,
          // isLike: 0,
          // createTime: "2026-04-04 22:13:05",
          // childReplies: null,
        };
      },
    },
  },
  data() {
    return {
      localDiscussionData: {
        id: "",
        userId: "",
        parentId: "",
        avatar: "",
        content: "",
        realName: "",
        count: 0,
        isLike: 0,
        createTime: "",
        childReplies: null,
      },
      currentUserId: null,
      currentRole: null
    };
  },
  watch: {
    discussionData: {
      handler(val) {
        this.localDiscussionData = val;
      },
      immediate: true
    }
  },
  created(){
    this.currentUserId = parseInt(getUserId())
    this.currentRole = getRole()
  },
  methods: {
    toggleLike() {
      this.localDiscussionData.isLike = this.localDiscussionData.isLike === 0 ? 1 : 0;
      const data = {
        "discussionId": this.discussionId,
        "replyId": this.localDiscussionData.id
      }
      doLike(data).then(() => {
        this.onConfirm();
      })
    },
    showIsDel(id){
      console.log(111111)
      this.delFun(id);

    },
  }
};
</script>
<style scoped>

.bg-green-500{
    color: #2ed892;
}
.bg-gray-300{
    color: #4b4b4b;
}
.author {
  display: inline-block;
  vertical-align: middle;
  font-size: 14px;
  color: #8a8b99;
  margin-right: 6px;
}
.topicDetail_info {
  position: relative;
  height: 28px;
  margin-bottom: 20px;
}
.topic_interactive {
  margin-top: 6px;
  height: 20px;
  line-height: 20px;
}
.topicReply_right {
  padding-left: 46px;
  font-size: 14px;
  line-height: 20px;
  color: #131b26;
}
.topicDetail_avatar {
  display: inline-block;
  vertical-align: middle;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  margin-right: 20px;
}
.topicDetail_replyItem {
  padding: 20px 30px 30px 50px;
  border-radius: 10px;
  /* border-bottom: solid 1px #1b1b1b; */

  background-color: #f6f6f6;
  .topicDetail_info {
    height: 36px;
    margin-bottom: 0;
    /* .topicDetail_avatar {
      width: 36px;
      height: 36px;
    } */

    .department {
      margin-left: 0;
      margin-right: 10px;
    }
    .author {
      margin-right: 14px;
    }
    .topic_interactive {
      position: absolute;
      right: 0;
      top: 4px;
      margin: 0;
      .like {
        float: left;
        text-align: left;
      }
      .comment {
        float: left;
        text-align: left;
      }
      .more {
        position: static;
        float: left;
        margin-left: -16px;
        margin-right: -8px;
      }
      .priceModal {
        display: none;
        position: absolute;
        padding: 24px 0;
        min-width: 275px;
        left: -114px;
        top: 22px;
        box-sizing: border-box;
        background: #ffffff;
        box-shadow: 0 2px 12px 0 rgba(190, 199, 206, 0.62);
        border-radius: 6px;
        z-index: 10;
        user-select: none;
      }
    }
  }
  .topicReply_right {
    padding-left: 50px;
    .otherInfo {
      font-size: 12px;
      color: #8a8b99;
      margin-bottom: 16px;
    }
  }
}
</style>
