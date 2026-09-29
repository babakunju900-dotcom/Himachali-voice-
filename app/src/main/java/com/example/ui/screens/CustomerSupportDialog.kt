package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SupportTicketEntity
import com.example.ui.theme.*

@Composable
fun CustomerSupportDialog(
    tickets: List<SupportTicketEntity>,
    onDismiss: () -> Unit,
    onSubmitTicket: (category: String, subject: String, message: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Payment") }
    var showNewTicketForm by remember { mutableStateOf(false) }

    val categories = listOf("Payment", "Account", "Room Issue", "Harassment", "Other")

    Surface(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        color = StarKingBgDark
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Official Customer Support 🎫",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "STAR KING 24/7 User Resolution Desk",
                        color = StarGoldLight,
                        fontSize = 11.sp
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("support_close_btn")) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextWhite)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action: Create ticket button
            Button(
                onClick = { showNewTicketForm = !showNewTicketForm },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (showNewTicketForm) StarKingCardDark else StarGoldPrimary,
                    contentColor = if (showNewTicketForm) TextWhite else StarKingBgDark
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("toggle_new_ticket_btn")
            ) {
                Text(
                    text = if (showNewTicketForm) "View My Tickets" else "+ Open New Support Ticket",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (showNewTicketForm) {
                // New Ticket Form
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "Inquiry Category", color = TextMuted, fontSize = 12.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { cat ->
                            val isSelected = cat == selectedCategory
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) StarGoldPrimary else StarKingSurfaceVariantDark)
                                    .clickable { selectedCategory = cat }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = cat,
                                    color = if (isSelected) StarKingBgDark else TextWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject", color = TextMuted) },
                        placeholder = { Text("e.g. Recharge delay or mic room issue", color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StarGoldPrimary,
                            unfocusedBorderColor = StarKingCardBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("ticket_subject_input")
                    )

                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Description", color = TextMuted) },
                        placeholder = { Text("Provide complete details for faster resolution...", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = StarGoldPrimary,
                            unfocusedBorderColor = StarKingCardBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("ticket_message_input")
                    )

                    Button(
                        onClick = {
                            if (subject.isNotBlank() && message.isNotBlank()) {
                                onSubmitTicket(selectedCategory, subject, message)
                                subject = ""
                                message = ""
                                showNewTicketForm = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StarGoldPrimary, contentColor = StarKingBgDark),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(46.dp).testTag("submit_ticket_btn")
                    ) {
                        Text("Submit Ticket", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            } else {
                // Tickets List
                Text(text = "My Submitted Tickets (${tickets.size})", color = TextMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                if (tickets.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No tickets yet. If you have an inquiry, open a new ticket!", color = TextMuted, fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(tickets, key = { it.ticketId }) { ticket ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = StarKingSurfaceVariantDark),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth().testTag("user_ticket_${ticket.ticketId}")
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = ticket.ticketId, color = StarGoldPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (ticket.status == "RESOLVED") LiveGreen.copy(alpha = 0.2f) else StarGoldPrimary.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = ticket.status,
                                                color = if (ticket.status == "RESOLVED") LiveGreen else StarGoldPrimary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "[${ticket.category}] ${ticket.subject}", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = ticket.message, color = TextChampagne, fontSize = 12.sp)

                                    if (ticket.staffReply.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(StarKingCardDark)
                                                .padding(8.dp)
                                        ) {
                                            Column {
                                                Text(text = "Official Staff Reply:", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                                Text(text = ticket.staffReply, color = TextWhite, fontSize = 11.sp)
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
