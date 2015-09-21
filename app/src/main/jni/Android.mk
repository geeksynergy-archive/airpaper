# Copyright (C) 2010 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := decomon

LOCAL_CFLAGS := -DANDROID_NDK \
                 -DDISABLE_IMPORTGL \
                 -Wall -g

 LOCAL_LDFLAGS          := -Wl,-Map,xxx.map

 LOCAL_SRC_FILES := \
    hdlc.c \
    demod_afsk12.c \
    pocsag.c \
    demod_dtmf.c \
    demod_poc5.c \
    demod_poc12.c \
    demod_poc24.c \
    costabf.c \
    costabi.c \
    multimon.c

 LOCAL_LDLIBS := -ldl -llog


include $(BUILD_SHARED_LIBRARY)

 

