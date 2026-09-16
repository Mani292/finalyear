package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FavoriteJunctionEntity
import com.example.ui.theme.TrafficAmberPrimary
import com.example.ui.theme.TrafficCyanAccent
import com.example.ui.theme.TrafficGreenSafe
import com.example.ui.theme.TrafficNavyCard
import com.example.ui.theme.TrafficNavyCardBorder
import com.example.ui.theme.TrafficNavyCardElevated
import com.example.ui.theme.TrafficTextMuted
import com.example.ui.theme.TrafficTextPrimary
import com.example.ui.theme.TrafficTextSecondary

@Composable
fun FavoriteJunctionsBar(
    favorites: List<FavoriteJunctionEntity>,
    selectedLocation: String,
    currentLocationName: String,
    onSelectFavorite: (FavoriteJunctionEntity) -> Unit,
    onRemoveFavorite: (String) -> Unit,
    onToggleCurrentFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCurrentSaved = favorites.any { it.junctionId == selectedLocation }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("favorite_junctions_quick_access_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TrafficNavyCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(
                if (isCurrentSaved) TrafficAmberPrimary.copy(alpha = 0.5f) else TrafficNavyCardBorder
            )
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Favorites",
                        tint = TrafficAmberPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "FAVORITE JUNCTIONS",
                        style = MaterialTheme.typography.labelSmall,
                        color = TrafficAmberPrimary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = TrafficAmberPrimary.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, TrafficAmberPrimary.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = "${favorites.size} PINNED (ROOM DB)",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TrafficAmberPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Quick Toggle Current Intersection Favorite Button
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isCurrentSaved) TrafficAmberPrimary.copy(alpha = 0.22f) else TrafficNavyCardElevated,
                    border = BorderStroke(
                        1.dp,
                        if (isCurrentSaved) TrafficAmberPrimary else TrafficNavyCardBorder
                    ),
                    modifier = Modifier
                        .clickable { onToggleCurrentFavorite() }
                        .testTag("btn_toggle_fav_current")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isCurrentSaved) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = if (isCurrentSaved) "Saved" else "Save current",
                            tint = if (isCurrentSaved) TrafficAmberPrimary else TrafficTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isCurrentSaved) "PINNED" else "+ PIN ACTIVE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrentSaved) TrafficAmberPrimary else TrafficTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (favorites.isEmpty()) {
                // Empty state card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = TrafficNavyCardElevated,
                    border = BorderStroke(1.dp, TrafficNavyCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleCurrentFavorite() }
                        .testTag("empty_favorites_card")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            tint = TrafficTextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "No favorite junctions saved yet in Room DB",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TrafficTextPrimary
                            )
                            Text(
                                text = "Tap here or on the Star icon above to pin $currentLocationName for quick 1-tap switching.",
                                fontSize = 11.sp,
                                color = TrafficTextMuted
                            )
                        }
                    }
                }
            } else {
                // Horizontal list of saved favorite junction cards
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(favorites, key = { it.junctionId }) { fav ->
                        val isActive = fav.junctionId == selectedLocation
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isActive) TrafficAmberPrimary.copy(alpha = 0.18f) else TrafficNavyCardElevated,
                            border = BorderStroke(
                                1.dp,
                                if (isActive) TrafficAmberPrimary else TrafficNavyCardBorder
                            ),
                            modifier = Modifier
                                .clickable { onSelectFavorite(fav) }
                                .testTag("favorite_chip_${fav.junctionId}")
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.widthIn(min = 140.dp, max = 220.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = TrafficAmberPrimary,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = fav.displayName,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isActive) TrafficAmberPrimary else TrafficTextPrimary,
                                            maxLines = 1
                                        )
                                    }

                                    // Remove favorite icon button
                                    IconButton(
                                        onClick = { onRemoveFavorite(fav.junctionId) },
                                        modifier = Modifier
                                            .size(20.dp)
                                            .testTag("btn_remove_favorite_${fav.junctionId}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove favorite",
                                            tint = TrafficTextMuted,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(5.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = TrafficCyanAccent.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "CAM ${fav.defaultCamera}",
                                            fontSize = 9.sp,
                                            color = TrafficCyanAccent,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = TrafficGreenSafe.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "DIR ${fav.defaultDirection}",
                                            fontSize = 9.sp,
                                            color = TrafficGreenSafe,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                    if (isActive) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = TrafficAmberPrimary.copy(alpha = 0.28f)
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                fontSize = 9.sp,
                                                color = TrafficAmberPrimary,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    } else {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = TrafficNavyCardBorder.copy(alpha = 0.6f)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.NearMe,
                                                    contentDescription = null,
                                                    tint = TrafficTextSecondary,
                                                    modifier = Modifier.size(8.dp)
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(
                                                    text = "SWITCH",
                                                    fontSize = 9.sp,
                                                    color = TrafficTextSecondary,
                                                    fontWeight = FontWeight.SemiBold
                                                )
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
