package com.spiphy.screentime.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.spiphy.screentime.R
import com.spiphy.screentime.model.StarGroup
import com.spiphy.screentime.model.testStarGroup


private val showAwardDialog = mutableStateOf(false)
private val showRedeemDialog = mutableStateOf(false)
private val selectedStarGroup = mutableStateOf<StarGroup?>(null)
private var onAwardStar: (starNote: String) -> Unit = {}
private var onRedeemStar: (starGroup: StarGroup, note: String) -> Unit = {_,_ ->}
private var onConverToScreenTime: () -> Unit = {}

@Composable
fun StarsScreen(
    viewModel: StarViewModel,
    retryAction: () -> Unit,
    contentPadding: PaddingValues
) {
    val uiState =viewModel.starUiState
    when (uiState) {
        is StarUiState.Loading -> LoadingScreen(
            contentPadding = contentPadding,
            modifier = Modifier.fillMaxSize()
        )

        is StarUiState.Success -> {
            onAwardStar = { note -> viewModel.awardStar(note) }
            onRedeemStar = { starGroup, note -> viewModel.redeemStar(starGroup, note) }
            onConverToScreenTime = { viewModel.convertToScreenTime() }
            Stars(
                uiState.starGroups, viewModel, contentPadding = contentPadding
            )
        }

        is StarUiState.Error -> Stars(
            testStarGroup, viewModel, contentPadding = contentPadding
        )
        //ErrorScreen(retryAction, modifier = modifier.fillMaxSize())

}

}

@Composable
fun Stars(
    starGroups: List<StarGroup>,
    viewModel: StarViewModel,
    contentPadding: PaddingValues
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAwardDialog.value = true }
            ) {
                Icon(Icons.Filled.Add, stringResource(id = R.string.award_ticket))
            }
        },
        modifier = Modifier.padding(contentPadding)
    ) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding,
        ) {
            items(starGroups) { starGroup ->
                StarGroup(
                    starGroup = starGroup,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp)
                )
            }
        }
        AwardStarDialog()
        RedeemStarDialog()
    }


}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StarGroup(
    starGroup: StarGroup,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val count = starGroup.stars.count()
    Card(
        modifier = modifier
            .combinedClickable(
                onClick = {
                    expanded = !expanded
                },
                onLongClick = {
                    if(!starGroup.used) {
                        selectedStarGroup.value = starGroup
                        showRedeemDialog.value = true
                    }
                }
            )
    ) {
        Row {
            Column(
                modifier = Modifier
                    .alpha(if (starGroup.used) 0.5f else 1.0f)
            ){
                Row{
                    for(i in 1..5) {
                        Image(
                            painter = painterResource(id = if (i <= count) R.drawable.star else R.drawable.star_outline),
                            contentDescription = null,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row {
                    for(i in 6..10) {
                        Image(
                            painter = painterResource(id = if (i <= count) R.drawable.star else R.drawable.star_outline),
                            contentDescription = null,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                if(expanded) {
                    starGroup.stars.forEach { star ->
                        Text(
                            modifier = Modifier.padding(2.dp),
                            text = "${star.note} - ${Utilities.starToTimeString(star)}"
                        )
                    }
                }

            }
        }
    }
}

@Composable
fun AwardStarDialog() {
    val options = listOf("Bed","Reading", "Glasses", "Custom")
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(options[0]) }
    var customText by remember { mutableStateOf("") }
    if (showAwardDialog.value) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text(text = stringResource(R.string.award_star))
            },
            text = {
                Column {
                    Column(Modifier.selectableGroup()) {
                        options.forEach { option ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = (option == selectedOption),
                                        onClick = { onOptionSelected(option) },
                                        role = Role.RadioButton
                                    )
                                    .padding(8.dp),
                            ) {
                                RadioButton(
                                    selected = (option == selectedOption),
                                    onClick = null
                                )
                                Text(
                                    text = option,
                                    modifier = Modifier
                                        .align(Alignment.CenterVertically)
                                        .padding(start = 8.dp)
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = customText,
                        onValueChange = {text -> customText = text},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        label = { Text(stringResource(R.string.custom_reason)) }
                    )
                }
            },

            confirmButton = {
                TextButton(onClick = {
                    showAwardDialog.value = false
                    var note = selectedOption
                    if(note == "Custom") {
                        note = customText
                    }
                    onAwardStar(note)
                }) {
                    Text(text = stringResource(R.string.award_star))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAwardDialog.value = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }
}


@Composable
fun RedeemStarDialog() {
    val options = listOf("Screen Time","Other")
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(options[0]) }
    var customText by remember { mutableStateOf("") }
    if (showRedeemDialog.value) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text(text = stringResource(R.string.redeem_star))
            },
            text = {
                Column {
                    Column(Modifier.selectableGroup()) {
                        options.forEach { option ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = (option == selectedOption),
                                        onClick = { onOptionSelected(option) },
                                        role = Role.RadioButton
                                    )
                                    .padding(8.dp),
                            ) {
                                RadioButton(
                                    selected = (option == selectedOption),
                                    onClick = null
                                )
                                Text(
                                    text = option,
                                    modifier = Modifier
                                        .align(Alignment.CenterVertically)
                                        .padding(start = 8.dp)
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = customText,
                        onValueChange = {text -> customText = text},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        label = { Text(stringResource(R.string.custom)) }
                    )
                }
            },

            confirmButton = {
                TextButton(onClick = {
                    showRedeemDialog.value = false
                    var note = customText
                    if(selectedStarGroup.value != null) {
                        if(selectedOption == "Screen Time") {
                            onConverToScreenTime()
                            note = "Screen Time"
                        }
                        onRedeemStar(selectedStarGroup.value!!, note)
                    }
                }) {
                    Text(text = stringResource(R.string.redeem_star))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRedeemDialog.value = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Preview
@Composable
fun StarsScreenPreview() {
    StarGroup(testStarGroup[0], Modifier)
}