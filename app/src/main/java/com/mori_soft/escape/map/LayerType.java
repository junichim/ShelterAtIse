/*
 * Copyright 2017 Junichi MORI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mori_soft.escape.map;

/**
 * 地図レイヤー種類.
 */
enum LayerType {
    Invalid,
    CurrentLocation,     // 現在位置
    SelectedShelter,     // 表示対象の避難所
    NonSelectedShelter, // 表示対象外の避難所
    NearestShelter,      // 最も近い避難所
    NearShelter,         // 近傍の避難所
    PathToShelter        // 避難所までの経路
}
