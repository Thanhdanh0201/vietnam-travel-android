package com.example.vietnam_travel_itinerary_android.ui.community

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vietnam_travel_itinerary_android.ui.theme.SlateGray900
import com.example.vietnam_travel_itinerary_android.ui.theme.VNRed

enum class ReportTarget { POST, COMMENT, USER }

private val reportReasons = listOf(
    "spam" to "Spam",
    "harassment" to "Quấy rối",
    "inappropriate" to "Nội dung không phù hợp",
    "misinformation" to "Thông tin sai lệch",
    "other" to "Khác",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportBottomSheet(
    target: ReportTarget,
    onDismiss: () -> Unit,
    onSubmit: (reason: String, description: String?) -> Unit,
) {
    var selectedReason by remember { mutableStateOf(reportReasons.first().first) }
    var description by remember { mutableStateOf("") }

    val title = when (target) {
        ReportTarget.POST -> "Báo cáo bài viết"
        ReportTarget.COMMENT -> "Báo cáo bình luận"
        ReportTarget.USER -> "Báo cáo người dùng"
    }

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = androidx.compose.ui.graphics.Color.White) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SlateGray900)
            Text("Chọn lý do báo cáo", style = MaterialTheme.typography.bodySmall)

            reportReasons.forEach { (value, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedReason == value,
                            onClick = { selectedReason = value },
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = selectedReason == value,
                        onClick = { selectedReason = value },
                        colors = RadioButtonDefaults.colors(selectedColor = VNRed),
                    )
                    Text(label)
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Mô tả thêm (tùy chọn)") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = { onSubmit(selectedReason, description.takeIf { it.isNotBlank() }) },
                colors = ButtonDefaults.buttonColors(containerColor = VNRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
            ) {
                Text("Gửi báo cáo", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
