import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

// Google Map
import { AgmCoreModule } from '@agm/core';
// Leaflet Map
import { LeafletModule } from '@asymmetrik/ngx-leaflet';

// Component pages
import { MapsRoutingModule } from './maps-routing.module';
import { SharedModule } from '../../shared/shared.module';
import { GoogleComponent } from './google/google.component';
import { LeafletComponent } from './leaflet/leaflet.component';
import { environment } from '../../../environments/environment';

@NgModule({
  declarations: [
    GoogleComponent,
    LeafletComponent
  ],
  imports: [
    CommonModule,
    AgmCoreModule.forRoot({
      apiKey: environment.googleMapsApiKey
    }),
    LeafletModule,
    MapsRoutingModule,
    SharedModule
  ]
})
export class MapsModule { }
