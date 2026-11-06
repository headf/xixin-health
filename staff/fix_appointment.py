# -*- coding: utf-8 -*-
import codecs

# Fix 1: Add getUserAppointments API
path = "D:/Desktop/学习课程/Course Design/physical-examination/staff/src/api/index.js"
with codecs.open(path, 'r', 'utf-8') as f:
    c = f.read()
c = c.rstrip() + '\n\nexport const getUserAppointments = (userId) =>\n  request.get(`/appointments/user/${userId}`)\n'
with codecs.open(path, 'w', 'utf-8') as f:
    f.write(c)
print("API added")

# Fix 2: Update Checkin.vue
path = "D:/Desktop/学习课程/Course Design/physical-examination/staff/src/views/nurse/Checkin.vue"
with codecs.open(path, 'r', 'utf-8') as f:
    c = f.read()

c = c.replace(
    "import { checkIn, getCheckedIn, getPatients, getProfilesByUser, getWeeklyTrend } from '@/api'",
    "import { checkIn, getCheckedIn, getPatients, getProfilesByUser, getWeeklyTrend, getUserAppointments } from '@/api'"
)

old_fn = """async function onPatientChange() {
  if (selectedPatient.value) {
    form.userId = String(selectedPatient.value)
    form.appointmentId = String(selectedPatient.value - 3)
    if (patientProfiles.value[selectedPatient.value]) {
      form.profileId = String(patientProfiles.value[selectedPatient.value])
    } else {
      try {
        const res = await getProfilesByUser(selectedPatient.value)
        if (res?.data?.length) {
          patientProfiles.value[selectedPatient.value] = res.data[0].id
          form.profileId = String(res.data[0].id)
        }
      } catch {}
    }
  }
}"""

new_fn = """async function onPatientChange() {
  if (selectedPatient.value) {
    form.userId = String(selectedPatient.value)
    // 从后端查询该用户的预约ID（不再硬编码）
    try {
      const res = await getUserAppointments(selectedPatient.value)
      if (res?.data?.length) {
        form.appointmentId = String(res.data[0].id)
      }
    } catch {}
    if (patientProfiles.value[selectedPatient.value]) {
      form.profileId = String(patientProfiles.value[selectedPatient.value])
    } else {
      try {
        const res = await getProfilesByUser(selectedPatient.value)
        if (res?.data?.length) {
          patientProfiles.value[selectedPatient.value] = res.data[0].id
          form.profileId = String(res.data[0].id)
        }
      } catch {}
    }
  }
}"""

c = c.replace(old_fn, new_fn)
with codecs.open(path, 'w', 'utf-8') as f:
    f.write(c)
print("Checkin.vue updated")
