package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ServiceType
import com.example.model.UiServiceLocation
import com.example.ui.TrafficUiState
import com.example.ui.theme.TrafficAmberPrimary
import com.example.ui.theme.TrafficCyanAccent
import com.example.ui.theme.TrafficDangerRed
import com.example.ui.theme.TrafficDarkCard
import com.example.ui.theme.TrafficDarkSurface
import com.example.ui.theme.TrafficEmeraldSuccess
import com.example.ui.theme.TrafficIndigoPrimary
import com.example.ui.theme.TrafficNavyCard
import com.example.ui.theme.TrafficNavyCardBorder
import com.example.ui.theme.TrafficNavyCardElevated
import com.example.ui.theme.TrafficNavyDark
import com.example.ui.theme.TrafficTextMuted
import com.example.ui.theme.TrafficTextPrimary
import com.example.ui.theme.TrafficTextSecondary
import com.example.ui.theme.TrafficWarningAmber

@Composable
fun ServicesScreen(
    uiState: TrafficUiState,
    onSelectFilter: (ServiceType) -> Unit,
    onNavigateBackToMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentRoute = uiState.routeOptions.getOrNull(uiState.selectedRouteIndex)
    val services = uiState.servicesAlongRoute

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TrafficNavyDark)
            .testTag("services_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Route Context Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("services_route_context_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = TrafficNavyCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, TrafficNavyCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "HIGHWAY & EMERGENCY SERVICES",
                            color = TrafficCyanAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = TrafficIndigoPrimary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${services.size} Nearby",
                                color = TrafficIndigoPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentRoute?.name ?: "Current Selected Route",
                        color = TrafficTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${uiState.bengaluruOrigin.name} ➔ ${uiState.bengaluruDestination.name}",
                        color = TrafficTextSecondary,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Emergency SOS quick strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(TrafficDangerRed.copy(alpha = 0.15f))
                            .border(1.dp, TrafficDangerRed.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = TrafficDangerRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Bengaluru Emergency Helplines",
                                color = TrafficDangerRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Ambulance: 108 • Traffic Police: 103 • SOS: 112",
                                color = TrafficTextPrimary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Service Category Filter Chips
        item {
            Column {
                Text(
                    text = "FILTER BY SERVICE CATEGORY",
                    color = TrafficTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ServiceType.values()) { type ->
                        val isSelected = type == uiState.selectedServiceType
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectFilter(type) },
                            label = { Text("${type.iconEmoji} ${type.label}") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TrafficCyanAccent.copy(alpha = 0.2f),
                                selectedLabelColor = TrafficCyanAccent,
                                containerColor = TrafficNavyCard,
                                labelColor = TrafficTextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) TrafficCyanAccent else TrafficNavyCardBorder
                            )
                        )
                    }
                }
            }
        }

        // Service Cards List
        items(services) { service ->
            ServiceCardItem(
                service = service,
                onShowOnMap = onNavigateBackToMap,
                onCall = {
                    try {
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${service.contact}"))
                        context.startActivity(dialIntent)
                    } catch (_: Exception) {
                        // Safe fallback in emulator
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ServiceCardItem(
    service: UiServiceLocation,
    onShowOnMap: () -> Unit,
    onCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (typeColor, typeIcon, typeLabel) = when (service.type) {
        ServiceType.HOSPITAL -> Triple(TrafficDangerRed, Icons.Default.LocalHospital, "Hospital")
        ServiceType.FUEL -> Triple(TrafficAmberPrimary, Icons.Default.EvStation, "Fuel & EV")
        ServiceType.RESTAURANT -> Triple(TrafficEmeraldSuccess, Icons.Default.Restaurant, "Food & Dining")
        ServiceType.PHARMACY -> Triple(TrafficCyanAccent, Icons.Default.LocalHospital, "Pharmacy")
        else -> Triple(TrafficCyanAccent, Icons.Default.LocationOn, "Service")
    }

    Card(
        modifier = modifier.testTag("service_card_${service.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TrafficNavyCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, TrafficNavyCardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Type Badge + Proximity Distance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = typeColor.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, typeColor.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = typeIcon,
                                contentDescription = null,
                                tint = typeColor,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = typeLabel.uppercase(),
                                color = typeColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (service.is24x7) {
                        Surface(
                            color = TrafficEmeraldSuccess.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "24/7 OPEN",
                                color = TrafficEmeraldSuccess,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                // Proximity
                Surface(
                    color = TrafficNavyDark,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TrafficNavyCardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NearMe,
                            contentDescription = null,
                            tint = TrafficCyanAccent,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${service.distanceFromRouteKm} km off route",
                            color = TrafficCyanAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Service Name
            Text(
                text = service.name,
                color = TrafficTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            // Area and tag
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${service.area} • ${service.tag}",
                color = TrafficTextSecondary,
                fontSize = 12.sp
            )

            // Contact
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = TrafficTextMuted,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = service.contact,
                    color = TrafficTextMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Show on Map & Call
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onShowOnMap,
                    modifier = Modifier.weight(1f).testTag("btn_map_${service.id}"),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TrafficCyanAccent.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = TrafficCyanAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Show on Map", color = TrafficCyanAccent, fontSize = 12.sp)
                }

                Button(
                    onClick = onCall,
                    modifier = Modifier.weight(1f).testTag("btn_call_${service.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = TrafficIndigoPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Contact", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}
