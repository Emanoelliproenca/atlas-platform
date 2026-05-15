import { Routes } from '@angular/router';
import { adminGuard } from './admin.guard';
import { CadastrosComponent } from './cadastros.component';
import { DetalheContratoComponent } from './detalhe-contrato.component';
import { DetalheServicoComponent } from './detalhe-servico.component';
import { HomeRouteComponent } from './home-route.component';
import { LoginRouteComponent } from './login-route.component';
import { RelacionamentosComponent } from './relacionamentos.component';
import { RepasseComponent } from './repasse.component';
import { SoftwaresComponent } from './softwares.component';

export const routes: Routes = [
  {
    path: '',
    component: HomeRouteComponent
  },
  {
    path: 'login',
    component: LoginRouteComponent
  },
  {
    path: 'contratos',
    redirectTo: 'relacionamentos',
    pathMatch: 'full'
  },
  {
    path: 'relacionamentos',
    component: RelacionamentosComponent
  },
  {
    path: 'cadastros',
    component: CadastrosComponent,
    canActivate: [adminGuard]
  },
  {
    path: 'repasse',
    component: RepasseComponent
  },
  {
    path: 'softwares',
    component: SoftwaresComponent
  },
  {
    path: 'contratos/:id',
    component: DetalheContratoComponent
  },
  {
    path: 'servicos/:id',
    component: DetalheServicoComponent
  },
  {
    path: '**',
    redirectTo: ''
  }
];
