package com.example.arfitness.entitiy

import java.io.Serializable


data class Exercise (val id: String,
                    val imageResId: Int,
                     val name: String,
                     val description: String) :Serializable {

}