// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.gxf.crestdeviceservice

import java.util.UUID

object FirmwareTestConstants {
    const val FIRMWARE_FILE_FULL_NAME = "RTU#FULL#TO#23.10.txt"

    const val FIRMWARE_FILE_DELTA_NAME = "RTU#DELTA#FROM#23.10#TO#24.00.txt"
    const val FIRMWARE_FILE_DELTA_VERSION = "24.00"
    const val FIRMWARE_FILE_DELTA_PREVIOUS_VERSION = "23.10"
    const val FIRMWARE_FILE_DELTA_PACKET_SIZE = 6

    const val FIRMWARE_VERSION = "01.10"
    const val FIRMWARE_FROM_VERSION = "01.20"
    const val FIRMWARE_NAME = "RTU#DELTA#FROM#$FIRMWARE_FROM_VERSION#TO#$FIRMWARE_VERSION"
    const val FIRMWARE_WRONG_NAME = "FOTA.txt"
    const val FIRMWARE_PACKET_0 =
        "OTA0000or^kx?6<yS=?v_sLE%07c5\$Q44bK-ZDiN~L0#M3`5CO0Nu?N}rC*rK{yx{@B9g?g)9S{gm0NN+;k=rLQmqGAk?RVnN0izHIpa8sqK#|lZP?6OqV3F1*aFKHm2(SRxCxjc73#JRV3yFi8gR6rv5D5SP&9~mSYyo=#;E~!V@R8dm@DK?Q05F#+@MP_G;t7?E0Rq4e?_v-MAOIil_5nbFQGsHCbAf=7)F(_32`~VVk<}-lk=7@\$k=G}L50xM05Dee|54I1I5hRb3hTwpc2`7({eh>`+01uK8B#)DylY_B=aFCG>Cq57j5CD-81;B_S8Ss(dkdq82fRo-34IlsxC!djzkq?j\$z7LQekQkNM5Dh>8g8PUkfccP-4=0%;De!?2KoAX300Y32;E<CICxDX=C!djzk!=tSkN|11X8UF*VEIg&2Ec%k3MT^)4WIyl5e&eD;0M4>n+U*wkqRfs5Dl;Zfe{42h2Vgb3MVO=K#?XVQxFZn0EOb0;trHkGXIh9k-w3zk\$eykPym3D3MPRO2f\$yL5Rw%Ff#Bs35O4s3;DD0~CMuXKm?@Z9nt+jk5D<_6fRPC&fe{wK4wQEikl=uo_Yfrz07uV&;0Tp~k!L4{m0Zn;4=xZTAOL`qXD48jX(vFFXeS7j|C09*B|rcOFYu6PClHZoC*s_J5fH%i5G7CmfRSe>1KoiU3&0;K5NIIc-4G>!0AS#7;L8Cdz<`lwCy!s|+`JGaumB<VfiH*;hzpE^kdbI7oG_jcCGY^ik!UBzmBy70lw4eE0ek@h5GDWsfRSk@lHkdofDI8OVc>A!{17tG0DzGcCy<d9C!moQC\$NzhCs+_N-~fp\$iYtpNA@v}V6elo}6(_zBGVlOEk`^aWk{2f%z%byD-~pGc5Hx@Q+LRBF50KFi)6amBA}4_nArLlj0C@volXwGwks>FEBQfxU;MWj25CA3MHsV9=THs;ecHj<_c?x6@Ij{iUw>*g8ksBuKFbME\$z\$XEf5IN8Q>0J6vAcHZ3LW5I-+yU&74*"
    val FIRMWARE_UUID = UUID.randomUUID()
    val PREVIOUS_FIRMWARE_UUID = UUID.randomUUID()
}
