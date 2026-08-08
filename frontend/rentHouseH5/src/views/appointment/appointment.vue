<template>
  <van-skeleton :row="20" :loading="loading">
    <div class="page-container min-h-[100vh] py-[15px]">
      <!--    预约公寓-->
      <div>
        <div class="base-info-title main-container py-[4px]">预约公寓</div>
        <div class="my-[5px] px-[15px]">
          <ApartmentCard :data="apartmentDetailInfo"></ApartmentCard>
        </div>
      </div>
      <!--    预约信息-->
      <div>
        <div class="base-info-title main-container py-[4px]">预约信息</div>
        <div class="my-[5px] relative">
          <van-form @submit="onSubmit">
            <van-cell-group inset>
              <!--            姓名-->
              <van-field
                v-model.trim="formData.name"
                name="name"
                label="姓名"
                autocomplete="off"
                placeholder="请输入姓名"
                :rules="[{ required: true, message: '请填写姓名' }]"
              />
              <!--            手机号-->
              <van-field
                v-model.trim="formData.phone"
                name="phone"
                label="手机号"
                type="tel"
                autocomplete="off"
                placeholder="请输入手机号"
                :rules="[
                  {
                    required: true,
                    pattern: /^1([3589]\d|4[5-9]|6[1-2,4-7]|7[0-8])\d{8}$/,
                    message: '请正确填写手机号'
                  }
                ]"
              />
              <!--            预约日期-->
              <van-field
                v-model="formData.date"
                is-link
                readonly
                name="date"
                label="预约日期"
                placeholder="点击选择日期"
                @click="showDate = true"
                :rules="[{ required: true, message: '请选择预约日期' }]"
              />
              <van-popup v-model:show="showDate" position="bottom">
                <van-date-picker
                  :min-date="new Date()"
                  @confirm="onConfirmAppointmentDateHandle"
                  @cancel="showDate = false"
                />
              </van-popup>
              <!--            预约时间-->
              <van-field
                v-model="formData.time"
                is-link
                readonly
                name="time"
                label="预约时间"
                placeholder="点击选择时间"
                @click="showTime = true"
                :rules="[{ required: true, message: '请选择预约时间' }]"
              />
              <van-popup v-model:show="showTime" position="bottom">
                <van-time-picker
                  v-model="dateInfo.time"
                  title="选择时间"
                  @confirm="onConfirmAppointmentTimeHandle"
                  @cancel="showTime = false"
                  :columns-type="['hour', 'minute', 'second']"
              /></van-popup>
              <!--            备注信息-->
              <van-field
                v-model.trim="formData.additionalInfo"
                name="additionalInfo"
                label="备注信息"
                rows="3"
                autosize
                type="textarea"
                autocomplete="off"
                placeholder="请输入备注信息"
                show-word-limit
                maxlength="50"
              />
            </van-cell-group>
            <div class="mt-[50px] main-container">
              <van-button round block type="primary" native-type="submit">
                {{ formData.id ? "重新预约" : "预约看房" }}
              </van-button>
            </div>
          </van-form>
        </div>
      </div>
      <div class="absolute bottom-0"></div>
    </div>
  </van-skeleton>
</template>
<script setup lang="ts">
import {
  getApartmentDetailById,
  getAppointmentDetailById,
  saveOrUpdateAppointment
} from "@/api/search";
import { onMounted, ref } from "vue";
import dayjs from "dayjs";
import type {
  ApartmentInterface,
  AppointmentQueryInterface
} from "@/api/search/types";
import { useRoute, useRouter } from "vue-router";
import ApartmentCard from "@/components/ApartmentCard/ApartmentCard.vue";
import { AppointmentStatus } from "@/enums/constEnums";
import { showToast } from "vant";

const route = useRoute();
const router = useRouter();

const loading = ref(true);
const apartmentDetailInfo = ref<ApartmentInterface>({} as ApartmentInterface);

const getApartmentDetailHandle = async () => {
  const { data } = await getApartmentDetailById(
    route.query.apartmentId as string
  );
  apartmentDetailInfo.value = data;
};

const formData = ref<
  AppointmentQueryInterface & { date: string; time: string }
>({
  id: "",
  name: "",
  phone: "",
  apartmentId: route.query.apartmentId || "",
  appointmentTime: "",
  additionalInfo: "",
  appointmentStatus: AppointmentStatus.WAITING,
  date: "",
  time: ""
});

const dateInfo = ref({
  date: [
    dayjs().get("year").toString(),
    (dayjs().get("month") + 1).toString().padStart(2, "0"),
    dayjs().get("date").toString().padStart(2, "0")
  ],
  time: [
    dayjs().get("hour").toString().padStart(2, "0"),
    dayjs().get("minute").toString().padStart(2, "0"),
    dayjs().get("second").toString().padStart(2, "0")
  ]
});

const syncAppointmentTime = () => {
  formData.value.date = dateInfo.value.date.join("-");
  formData.value.time = dateInfo.value.time.join(":");
  formData.value.appointmentTime = `${formData.value.date}T${formData.value.time}.000+08:00`;
};

const showDate = ref(false);
const onConfirmAppointmentDateHandle = (date: any) => {
  dateInfo.value.date = date.selectedValues;
  syncAppointmentTime();
  showDate.value = false;
};

const showTime = ref(false);
const onConfirmAppointmentTimeHandle = (time: any) => {
  dateInfo.value.time = time.selectedValues;
  syncAppointmentTime();
  showTime.value = false;
};

async function onSubmit() {
  const { date, time, ...params } = formData.value;
  await saveOrUpdateAppointment(params as AppointmentQueryInterface);
  showToast("操作成功");
  route.query.apartmentId
    ? await router.replace("/myAppointment")
    : await router.back();
}

async function getAppointmentDetailHandle() {
  const { data } = await getAppointmentDetailById(
    route.query.appointmentId as string
  );
  formData.value.name = data.name;
  formData.value.phone = data.phone;
  formData.value.additionalInfo = data.additionalInfo;
  formData.value.appointmentStatus = data.appointmentStatus;
  formData.value.id = data.id;
  formData.value.apartmentId = String(data.apartmentId);

  const timeStr = data.appointmentTime;
  const [datePart, timePartRaw] = timeStr.includes("T")
    ? timeStr.split("T")
    : timeStr.split(" ");
  const timePart = timePartRaw.substring(0, 8);
  dateInfo.value.date = datePart.split("-");
  dateInfo.value.time = timePart.split(":");
  syncAppointmentTime();

  if (data.apartmentItemVo && data.apartmentItemVo.id) {
    apartmentDetailInfo.value = data.apartmentItemVo;
  } else if (data.apartmentId) {
    const { data: apartmentData } = await getApartmentDetailById(String(data.apartmentId));
    apartmentDetailInfo.value = apartmentData;
  }
}

onMounted(async () => {
  try {
    await Promise.all([
      route.query.apartmentId && getApartmentDetailHandle(),
      route.query.appointmentId && getAppointmentDetailHandle()
    ]);
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped lang="less">
.base-info-title {
  font-weight: bold;
}
::v-deep .van-card {
  background: var(--van-background-2) !important;
}
</style>
