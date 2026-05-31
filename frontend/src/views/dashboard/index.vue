
<template>
  <div>
    <div>
      <com-admin v-if="url == 0" />
      <com-teacher v-if="url == 1" />
      <com-student v-else-if="url == 2" />
    </div>
  </div>
</template>
<script>
import comAdmin from './rolePage/admin.vue'
import comTeacher from './rolePage/teacher.vue'
import comStudent from './rolePage/student.vue'
export default {
  components: {
    comAdmin,
    comTeacher,
    comStudent
  },
  data() {
    return {
      url: 0

    }
  },
  created() {
    // this.url = this.$route.query.url;
    const roles = localStorage.getItem('class_activity_system_role')
    switch (roles) {
      case 'admin': {
        this.url = 0
        break
      }
      case 'teacher': {
        this.url = 1
        break
      }
      case 'student': {
        this.url = 2
        break
      }
    }
  }

}
</script>
<style scoped>

</style>
