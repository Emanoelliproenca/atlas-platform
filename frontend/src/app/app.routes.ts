import { Routes } from '@angular/router';
import { adminGuard } from './admin.guard';
import { authGuard, loginRedirectGuard } from './auth.guard';
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
    component: HomeRouteComponent,
    canActivate: [authGuard]
  },
  {
    path: 'login',
    component: LoginRouteComponent,
    canActivate: [loginRedirectGuard]
  },
  {
    path: 'contratos',
    redirectTo: 'relacionamentos',
    pathMatch: 'full'
  },
  {
    path: 'relacionamentos',
    component: RelacionamentosComponent,
    canActivate: [authGuard]
  },
  {
    path: 'cadastros',
    component: CadastrosComponent,
    canActivate: [authGuard, adminGuard]
  },
  {
    path: 'repasse',
    component: RepasseComponent,
    canActivate: [authGuard]
  },
  {
    path: 'softwares',
    component: SoftwaresComponent,
    canActivate: [authGuard]
  },
  {
    path: 'contratos/:id',
    component: DetalheContratoComponent,
    canActivate: [authGuard]
  },
  {
    path: 'servicos/:id',
    component: DetalheServicoComponent,
    canActivate: [authGuard]
  },
  {
    path: '**',
    redirectTo: ''
  }
];
