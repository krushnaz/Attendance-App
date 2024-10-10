const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

/**
 * Marks absent students for lectures that have ended.
 * @param {Object} context - The context object provided by Firebase functions.
 */
exports.markAbsentees = functions.pubsub
    .schedule("every 15 minutes") // Runs every 15 minutes
    .onRun(async (context) => {
      const db = admin.database();
      const now = new Date();
      const currentTime = `${now.getHours()}:${now.getMinutes()}`;
      const formattedDate = formatDate(now);

      // Fetch all lectures
      const lecturesRef = db.ref("timetables"); // Change to your actual path
      const lecturesSnapshot = await lecturesRef.once("value");
      const subjects = lecturesSnapshot.val();

      for (const courseName in subjects) {
        if (Object.prototype.hasOwnProperty.call(subjects, courseName)) {
          const semesters = subjects[courseName];

          for (const semester in semesters) {
            if (Object.prototype.hasOwnProperty.call(semesters, semester)) {
              const divisions = semesters[semester];

              for (const division in divisions) {
                if (Object.prototype.hasOwnProperty.call(divisions, division)) {
                  const lectures = divisions[division];

                  for (const lectureId in lectures) {
                    if (
                      Object.prototype.hasOwnProperty.call(
                          lectures,
                          lectureId,
                      )
                    ) {
                      const lecture = lectures[lectureId];

                      // Check if the lecture has ended
                      if (
                        lecture.date === formattedDate &&
                      lecture.endTime <= currentTime
                      ) {
                        const {userClass} = lecture;

                        // Fetch all users (students) in the class and division
                        const usersRef = db
                            .ref("users")
                            .orderByChild("userClass")
                            .equalTo(userClass);
                        const usersSnapshot = await usersRef.once("value");
                        const users = usersSnapshot.val();

                        for (const userId in users) {
                          if (
                            Object.prototype.hasOwnProperty.call(
                                users,
                                userId,
                            )
                          ) {
                            const user = users[userId];

                            if (user.division === division) {
                            // Check if the student has marked attendance
                              const attendanceRef = db
                                  .ref("attendance")
                                  .child(courseName)
                                  .child(semester)
                                  .child(division)
                                  .child(user.rollNumber);

                              const attendanceSnapshot =
                              await attendanceRef.once("value");
                              const attendanceRecords =
                              attendanceSnapshot.val();

                              let attendanceMarked = false;


                              for (const attendanceId in attendanceRecords) {
                                if (
                                  Object.prototype.hasOwnProperty.call(
                                      attendanceRecords,
                                      attendanceId,
                                  )
                                ) {
                                  const record =
                                  attendanceRecords[attendanceId];

                                  if (record.status === "Present") {
                                    attendanceMarked = true;
                                    break;
                                  }
                                }
                              }

                              // If the student hasn't marked attendance,
                              // mark them as "Absent"
                              if (!attendanceMarked) {
                                await db
                                    .ref("attendance")
                                    .child(courseName)
                                    .child(semester)
                                    .child(division)
                                    .child(user.rollNumber)
                                    .push({
                                      attendanceId: db.ref().push().key,
                                      courseName: courseName,
                                      date: formattedDate,
                                      division: division,
                                      status: "Absent",
                                      studentName: user.fullName,
                                      studentRollNo: user.rollNumber,
                                      subjectName: lecture.subjectName,
                                      teacherName: lecture.teacherName,
                                      time: currentTime,
                                    });
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    });

/**
 * Helper function to format the date as dd-MM-yyyy
 * @param {Date} date - The date to be formatted.
 * @return {string} - The formatted date string.
 */
function formatDate(date) {
  const day = String(date.getDate()).padStart(2, "0");
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const year = date.getFullYear();
  return `${day}-${month}-${year}`;
}

