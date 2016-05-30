/*
 *      multimon.h -- Monitor for many different modulation formats
 *
 *      Copyright (C) 1996  
 *          Thomas Sailer (sailer@ife.ee.ethz.ch, hb9jnx@hb9w.che.eu)
 *
 *      This program is free software; you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation; either version 2 of the License, or
 *      (at your option) any later version.
 *
 *      This program is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with this program; if not, write to the Free Software
 *      Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/* ---------------------------------------------------------------------- */

#ifndef _MULTIMON_H
#define _MULTIMON_H

#include <android/log.h>
#include <stdbool.h>
#include <stdint.h>

#define LOG_TAG "MultimonHamDroid"
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)


/* ---------------------------------------------------------------------- */

extern const float costabf[0x400];
#define COS(x) costabf[(((x)>>6)&0x3ffu)]
#define SIN(x) COS((x)+0xc000)

enum
{
    POCSAG_MODE_AUTO = 0,
    POCSAG_MODE_NUMERIC = 1,
    POCSAG_MODE_ALPHA = 2,
    POCSAG_MODE_SKYPER = 3,
};
/* ---------------------------------------------------------------------- */

struct demod_state {
	const struct demod_param *dem_par;
	union {
		struct l2_state_hdlc {
			unsigned char rxbuf[1024];
			unsigned char *rxptr;
			unsigned int rxstate;
			unsigned int rxbitstream;
			unsigned int rxbitbuf;
		} hdlc;


		struct l2_state_pocsag {
			unsigned int rx_data;
			unsigned char state;        // state machine
			unsigned char rx_bit;       // bit counter, counts 32bits
			unsigned char rx_word;
			signed int function;          // POCSAG function
			signed int address;           // POCSAG address
			unsigned char buffer[1024];
			unsigned int numnibbles;
			unsigned int pocsag_total_error_count;
			unsigned int pocsag_corrected_error_count;
			unsigned int pocsag_corrected_1bit_error_count;
			unsigned int pocsag_corrected_2bit_error_count;
			unsigned int pocsag_uncorrected_error_count;
			unsigned int pocsag_total_bits_received;
			unsigned int pocsag_bits_processed_while_synced;
			unsigned int pocsag_bits_processed_while_not_synced;
		} pocsag;
	} l2;
	
	
	union {
		struct l1_state_poc5 {
			unsigned int dcd_shreg;
			unsigned int sphase;
			unsigned int subsamp;
		} poc5;

		struct l1_state_poc12 {
			unsigned int dcd_shreg;
			unsigned int sphase;
			unsigned int subsamp;
		} poc12;

		struct l1_state_poc24 {
			unsigned int dcd_shreg;
			unsigned int sphase;
			unsigned int subsamp;

		} poc24;

		struct l1_state_afsk12 {
			unsigned int dcd_shreg;
			unsigned int sphase;
			unsigned int lasts;
			unsigned int subsamp;
		} afsk12;

		struct l1_state_afsk24 {
			unsigned int dcd_shreg;
			unsigned int sphase;
			unsigned int lasts;
		} afsk24;

		struct l1_state_hapn48 {
			unsigned int shreg;
			unsigned int sphase;
			float lvllo, lvlhi;
		} hapn48;

		struct l1_state_fsk96 {
			unsigned int dcd_shreg;
			unsigned int sphase;
			unsigned int descram;
		} fsk96;

		struct l1_state_dtmf {
			unsigned int ph[8];
			float energy[4];
			float tenergy[4][16];
			int blkcount;
			int lastch;
		} dtmf;

		struct l1_state_zvei {
			unsigned int ph[16];
			float energy[4];
			float tenergy[4][32];
			int blkcount;
			int lastch;
		} zvei;

		
		struct l1_state_morse {
			uint64_t current_sequence;
			int_fast16_t threshold_ctr;
			int_fast32_t detection_threshold;
			int_fast32_t filtered;
			int_fast32_t samples_since_change;
			int_fast32_t signal_max;
			int_fast32_t glitches;
			int_fast32_t erroneous_chars;
			int_fast32_t decoded_chars;
			int_fast16_t time_unit_dit_dah_samples;
			int_fast16_t time_unit_gaps_samples;
			int_fast16_t lowpass_strength;
			int_fast16_t holdoff_samples;
			int_fast8_t current_state;  // High = 1, Low = 0
		} morse;
		
		
/* 		struct l1_state_scope { */
/* 			int datalen; */
/* 			int dispnum; */
/* 			float data[1024]; */
/* 		} scope; */
	} l1;
};

struct demod_param {
	const char *name;
	bool float_samples; // if false samples are short instead
	unsigned int samplerate;
	unsigned int overlap;
	void (*init)(struct demod_state *s);
	void(*demod)(struct demod_state *s, float *buffer, int length);
	void(*deinit)(struct demod_state *s);
};

/* ---------------------------------------------------------------------- */

extern const struct demod_param demod_afsk1200;
extern const struct demod_param demod_poc24;
extern const struct demod_param demod_poc12;
extern const struct demod_param demod_poc5;
extern const struct demod_param demod_morse;
extern const struct demod_param demod_dtmf;

//extern const struct demod_param demod_scope;

#define ALL_DEMOD &demod_poc5, &demod_poc12, &demod_poc24, &demod_eas, &demod_ufsk1200, &demod_clipfsk, &demod_fmsfsk, \
    &demod_afsk1200, &demod_afsk2400, &demod_afsk2400_2, &demod_afsk2400_3, &demod_hapn4800, \
    &demod_fsk9600, &demod_dtmf, &demod_zvei1, &demod_zvei2, &demod_zvei3, &demod_dzvei, \
    &demod_pzvei, &demod_eea, &demod_eia, &demod_ccir, &demod_morse, &demod_dumpcsv SCOPE_DEMOD


/* ---------------------------------------------------------------------- */

void verbprintf(int verb_level, const char *fmt, ...);

void hdlc_init(struct demod_state *s);
void hdlc_rxbit(struct demod_state *s, int bit);


void pocsag_init(struct demod_state *s);
void pocsag_rxbit(struct demod_state *s, int bit);
void pocsag_deinit(struct demod_state *s);

// TODO this can't be a constant. FIXME
#define NAMED_PIPE "/data/data/com.geksynergy.airpaper/pipe"

/* ---------------------------------------------------------------------- */
#endif /* _MULTIMON_H */
